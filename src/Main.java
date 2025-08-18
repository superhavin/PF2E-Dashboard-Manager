import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;

public class Main {
    private final String url = "jdbc:mysql://127.0.0.1:3306/PATH";
    private final String user = "shelyn";
    private final String password = "The Gardens of Shelyn";

    // -------------------- SCHEMAS --------------------

    //size
    static String sizeSchema =
            "CREATE TABLE IF NOT EXISTS Size(" +
                    "size_type VARCHAR(32) UNIQUE NOT NULL, " +
                    "UNIQUE KEY Size(size_type) " +
                    "); ";

    //ability boost
    static String abilityBoostSchema =
            "CREATE TABLE IF NOT EXISTS AbilityBoost(" +
                    "ability_boost DECIMAL(2,1) DEFAULT 0, " +
                    "UNIQUE KEY ability_boost(ability_boost)" +
                    ");";


    //ability list
    static String abilitySchema =
            "CREATE TABLE IF NOT EXISTS Ability(" +
                    "ability_name VARCHAR(32) NOT NULL, " +
                    "UNIQUE KEY Ability(ability_name) " +
                    "); " +
                    //ability score list
                    "CREATE TABLE IF NOT EXISTS AbilityScore(" +
                    "ability_name VARCHAR(32) NOT NULL, " +
                    "ability_boost DECIMAL(2,1) DEFAULT 0, " +
                    "UNIQUE KEY ability_score(ability_name, ability_boost), " +
                    "FOREIGN KEY (ability_name) REFERENCES Ability(ability_name), " +
                    "FOREIGN KEY (ability_boost) REFERENCES AbilityBoost(ability_boost) " +
                    "); ";

    //proficiency
    static String proficiencySchema =
            "CREATE TABLE IF NOT EXiSTS Proficiency(" +
                    "proficiency_rank VARCHAR(32) UNIQUE NOT NULL, " +
                    "UNIQUE KEY Proficiency(proficiency_rank) " +
                    "); ";

    //skills list
    static String skillSchema =
            "CREATE TABLE IF NOT EXiSTS Skill(" +
                    "skill_name VARCHAR(32) NOT NULL, " +
                    "UNIQUE KEY Skill(skill_name) " +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS SkillRank(" +
                    "skill_name VARCHAR(32) NOT NULL, " +
                    "proficiency_rank VARCHAR(32) NOT NULL, " +
                    "UNIQUE KEY skill_rank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name), " +
                    "FOREIGN KEY (proficiency_rank) REFERENCES Proficiency(proficiency_rank) " +
                    "); ";

    //features list
    static String featureSchema =
            "CREATE TABLE IF NOT EXISTS Feature(" +
                    "feature_name VARCHAR(32) UNIQUE NOT NULL, " +
                    "UNIQUE KEY Feature(feature_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS FeatureRank(" +
                    "feature_name VARCHAR(32) NOT NULL, " +
                    "proficiency_rank VARCHAR(32) NOT NULL, " +
                    "UNIQUE KEY FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    //ancestry of character
    static String ancestrySchema =
            "CREATE TABLE IF NOT EXISTS Ancestry(" +
                    "ancestry_name VARCHAR(32) UNIQUE NOT NULL, " + //Unique Name = No Level
                    "hit_points INT NOT NULL, " +
                    "size VARCHAR(32) NOT NULL, " +
                    "speed INT NOT NULL, " +
                    "languages JSON NOT NULL, " + //JSON_ARRAY{"Common", "Orcish"}
                    "vision ENUM('low-light', 'dark', 'greater dark') DEFAULT NULL, " + //if null, normal vision

                    "CONSTRAINT size_limit CHECK(NOT(size = 'huge' OR size = 'gargantuan')), " + //no PCs can be ('huge'), ('gargantuan')

                    "UNIQUE KEY Ancestry(ancestry_name), " +
                    "FOREIGN KEY (size) REFERENCES Size(size_type) " +
                    ");" +

                    "CREATE TABLE IF NOT EXISTS AncestryBoost(" +
                    "ancestry_name VARCHAR(32) NOT NULL, " +
                    "ability_name VARCHAR(32) DEFAULT NULL, " + //if null = free boost
                    "ability_flaw BOOLEAN NOT NULL, " +

                    "CONSTRAINT free_or_flaw CHECK(NOT(ability_flaw AND ability_name IS NULL)), " + //boost can not be flaws or free

                    //"UNIQUE KEY(ancestry_name, ability_name), " + //unique, even if human have 2 free boosts
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name), " +
                    "FOREIGN KEY (ability_name) REFERENCES Ability(ability_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS AncestryHeritage(" +
                    "ancestry_name VARCHAR(32) NOT NULL, " +
                    "heritage_name varchar(32) NOT NULL, " +
                    "reference_title JSON DEFAULT NULL, " + //JSON_OBJECT{"GeneralFeat":"Cat Fall", "SpellList":"Innate Cantrip", "Action":"Call on Ancient Blood"}

                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(feature_proficiency = 'untrained')), " +  //no setting untrained prof.

                    "UNIQUE KEY AncestryHeritage(ancestry_name, heritage_name), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    "); ";

    static String backgroundSchema =
            "CREATE TABLE IF NOT EXISTS Background(" +
                    "background_name VARCHAR(32) UNIQUE NOT NULL, " +
                    "lore_name VARCHAR(32) DEFAULT NULL, " + //if null = player-chosen (aka free) boost
                    "reference_title JSON DEFAULT NULL, " + //JSON_OBJECT{"GeneralFeat":"Forager", "GeneralFeat":"Assurance"}

                    "CONSTRAINT background_title PRIMARY KEY (background_name)," +
                    "FOREIGN KEY (lore_name) REFERENCES Skill(skill_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS BackgroundSkill(" + //choices of skills from background
                    "background_name VARCHAR(32) NOT NULL, " +
                    "skill_name VARCHAR(32) DEFAULT NULL, " +

                    "additional_skill BOOLEAN NOT NULL, " + //if this skill is in addition to whatever skill you get from the background

                    "UNIQUE KEY (background_name, skill_name), " +
                    "FOREIGN KEY (background_name) REFERENCES Background(background_name), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS BackgroundBoost(" +
                    "background_name VARCHAR(32) NOT NULL, " +
                    "ability_boost VARCHAR(32) DEFAULT NULL, " + //if null = player-chosen (aka free) boost

                    "UNIQUE KEY (background_name, ability_boost), " +
                    "FOREIGN KEY (background_name) REFERENCES Background(background_name), " +
                    "FOREIGN KEY (ability_boost) REFERENCES Ability(ability_name) " +
                    "); ";


    //class of character
    static String classSchema =
            "CREATE TABLE IF NOT EXISTS Class(" +
                    "class_name VARCHAR(32) UNIQUE NOT NULL, " + //Unique Name = No Level
                    "key_ability VARCHAR(32) NOT NULL, " +
                    "hit_points INT NOT NULL DEFAULT 6, " +
                    "additional_skills INT NOT NULL DEFAULT 2, " +
                    "secondary_ability VARCHAR(32) DEFAULT NULL, " +

                    "UNIQUE KEY class(class_name), " +
                    "FOREIGN KEY (key_ability) REFERENCES Ability(ability_name), " +
                    "FOREIGN KEY (secondary_ability) REFERENCES Ability(ability_name)" +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS ClassProficiency(" + //all features as Untrained, unless class specifies otherwise
                    "class_name VARCHAR(32) NOT NULL, " +
                    "feature_name VARCHAR(32) NOT NULL, " + //INSERT all features
                    "feature_proficiency VARCHAR(32) DEFAULT 'untrained', " +

                    "UNIQUE KEY(class_name, feature_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS ClassSkill(" +
                    "class_name VARCHAR(32) NOT NULL, " +
                    "skill_name VARCHAR(32) NOT NULL, " +
                    "additional_skill BOOLEAN NOT NULL, " +

                    "UNIQUE KEY (class_name, skill_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS ClassOption(" + //class options pointing to additional abilities
                    "class_name VARCHAR(32) NOT NULL, " +
                    "option_level INT NOT NULL, " +
                    "class_option VARCHAR(32) DEFAULT NULL, " + //if class option = NULL, then it is a default option

                    "reference_title JSON DEFAULT NULL, " + //JSON_OBJECT{"GeneralFeat":"ShieldBlock", "ClassFeat":"Reactive Strike"} //{"ClassOption":"Spell Blending"}
                    "trained_skills JSON DEFAULT NULL, " + //JSON_ARRAY{'Diplomacy', 'Deception'}
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(feature_proficiency = 'untrained')), " +  //no setting untrained prof.

                    //"UNIQUE KEY ClassOption(class_name, option_level, class_option), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    //"FOREIGN KEY (skill_name) REFERENCES Skill(skill_name), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    "); ";

    //tables of actions
    static String actionSchema =
            "CREATE TABLE IF NOT EXISTS Action(" +
                    "action_name VARCHAR(32) NOT NULL, " +
                    "action_level INT NOT NULL, " +

                    "frequency VARCHAR(32) DEFAULT NULL, " +
                    "action ENUM('one-action', 'two-action', 'three-action', " +
                    "'reaction', 'free-action', 'first-action to three-action', " +
                    "'first-action or two-action', 'two-action or three-action') DEFAULT NULL, " +
                    "`trigger` TEXT DEFAULT NULL, " +
                    "requirement TEXT DEFAULT NULL, " +

                    "CONSTRAINT action_title PRIMARY KEY (action_name, action_level)" +
                    ");";

    //tables of source, all things have sources
    static String sourceSchema =
            "CREATE TABLE IF NOT EXISTS Source(" +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "name VARCHAR(32) NOT NULL, " +
                    "level INT DEFAULT NULL, " + //if null, level = 0

                    "book VARCHAR(64) NOT NULL, " +
                    "page INT NOT NULL, " +
                    "description TEXT NOT NULL, " + //Descriptions which refer to other feats/spells, format it like this: 'Fireball'

                    "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                    "traits JSON DEFAULT NULL, " + //JSON_ARRAY{"General", "Skill"}
                    "prerequisites JSON DEFAULT NULL, " + //JSON_OBJECT{"ClassFeat":"Aggressive Block", "ClassFeat":"Brutish Shove"}

                    "PRIMARY KEY (id), " +
                    "UNIQUE KEY title (name, level), " +
                    //"UNIQUE KEY citation_detail (book, page), " +
                    "UNIQUE KEY description_summary (description(256)) " +
                    ");";

    static String traditionSchema =
            "CREATE TABLE IF NOT EXISTS Tradition(" +
                    "tradition_name VARCHAR(32) NOT NULL, " +
                    "UNIQUE KEY SpellList(tradition_name) " +
                    ");";

    //table of spells... (JOIN with ActionTable)
    static String spellSchema = //needs spell slots/charges/focus table, spell abilities (for innate spells)
            "CREATE TABLE IF NOT EXISTS Spell(" +
                    "spell_name VARCHAR(32) NOT NULL, " +
                    "spell_rank INT NOT NULL, " +
                    "tradition VARCHAR(32) NOT NULL, " +

                    "`range` INT DEFAULT NULL, " +
                    "target varchar(64) DEFAULT NULL, " + //'creatures', 'allies', etc.
                    "area varchar(64) DEFAULT NULL, " + //20-foot burst
                    "defense varchar(64) DEFAULT NULL, " +
                    "cast varchar(64) DEFAULT NULL, " +
                    "duration INT DEFAULT NULL, " +
                    "heightened JSON DEFAULT NULL, " + //JSON_ARRAY{"Heightened (+1) The damage increases by 2d6."} //JSON_ARRAY{"Heightened (4th) You gain a +1 item bonus to saving throws.", "Heightened (6th) The item bonus to AC increases to +2, and you gain a +1 item bonus to saving throws."}

                    "CONSTRAINT spell_title PRIMARY KEY (spell_name, spell_rank, tradition), " +
                    "FOREIGN KEY (tradition) REFERENCES Tradition(tradition_name) " +
                    ");";

    static String spellListSchema = //spell list for classes with spells
            "CREATE TABLE IF NOT EXISTS SpellList(" +
                    "name VARCHAR(32) NOT NULL, " + //class or archetype name
                    "tradition VARCHAR(32) NOT NULL, " +
                    "list_type ENUM('prepared', 'spontaneous', 'focus', 'innate') DEFAULT 'innate', " +
                    "spellcasting_ability VARCHAR(32) DEFAULT NULL, " +

                    "spells_per_level INT DEFAULT 0, " +
                    "cantrips INT DEFAULT 5, " +
                    "granted_spell JSON DEFAULT NULL, " + //JSON_OBJECT{"Spell":"Fireball"}

                    "CONSTRAINT spell_list_title PRIMARY KEY (name, tradition, list_type), " +
                    "FOREIGN KEY (tradition) REFERENCES Tradition(tradition_name)," +
                    "FOREIGN KEY (spellcasting_ability) REFERENCES Ability(ability_name) " +
                    ");";

    //class feats
    static String classFeatSchema =
            "CREATE TABLE IF NOT EXISTS ClassFeat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +
                    "class_name VARCHAR(32) NOT NULL, " +

                    "ability_name VARCHAR(32) DEFAULT NULL, " +
                    "ability_boost DECIMAL (2,0) DEFAULT NULL, " +
                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "skill_proficiency VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(skill_proficiency = 'untrained' OR feature_proficiency = 'untrained')), " +

                    "CONSTRAINT class_feat_title PRIMARY KEY (feat_name, feat_level, class_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (ability_name, ability_boost) REFERENCES AbilityScore(ability_name, ability_boost), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    //ancestry feats
    static String ancestryFeatSchema =
            "CREATE TABLE IF NOT EXISTS AncestryFeat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +
                    "ancestry_name VARCHAR(32) NOT NULL, " +

                    "ability_name VARCHAR(32) DEFAULT NULL, " +
                    "ability_boost DECIMAL (2,0) DEFAULT NULL, " +
                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "skill_proficiency VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(skill_proficiency = 'untrained' OR feature_proficiency = 'untrained')), " +

                    "CONSTRAINT ancestry_feat_title PRIMARY KEY (feat_name, feat_level, ancestry_name), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name), " +
                    "FOREIGN KEY (ability_name, ability_boost) REFERENCES AbilityScore(ability_name, ability_boost), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    //general feats
    static String generalFeatSchema =
            "CREATE TABLE IF NOT EXISTS GeneralFeat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +

                    "ability_name VARCHAR(32) DEFAULT NULL, " +
                    "ability_boost DECIMAL (2,0) DEFAULT NULL, " +
                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "skill_proficiency VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(skill_proficiency = 'untrained' OR feature_proficiency = 'untrained')), " +

                    "CONSTRAINT general_feat_title PRIMARY KEY (feat_name, feat_level), " +
                    "FOREIGN KEY (ability_name, ability_boost) REFERENCES AbilityScore(ability_name, ability_boost), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    //skill feats
    static String skillFeatSchema =
            "CREATE TABLE IF NOT EXISTS SkillFeat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +
                    "skill_name VARCHAR(32) NOT NULL, " +
                    "skill_proficiency VARCHAR(32) NOT NULL, " +

                    "ability_name VARCHAR(32) DEFAULT NULL, " +
                    "ability_boost DECIMAL (2,0) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(skill_proficiency = 'untrained' OR feature_proficiency = 'untrained')), " +

                    "CONSTRAINT skill_feat_title PRIMARY KEY (feat_name, feat_level, skill_name), " +
                    "FOREIGN KEY (ability_name, ability_boost) REFERENCES AbilityScore(ability_name, ability_boost), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    //archetypes feats
    static String archetypeFeatSchema =
            "CREATE TABLE IF NOT EXISTS ArchetypeFeat(" +
                    "feat_name VARCHAR(32) NOT NULL UNIQUE, " +
                    "feat_level INT NOT NULL, " +
                    "archetype_dedication BOOLEAN NOT NULL, " +

                    "ancestry_name VARCHAR(32) DEFAULT NULL, " +
                    "class_name VARCHAR(32) DEFAULT NULL, " +
                    "ability_name VARCHAR(32) DEFAULT NULL, " + //need to allow multiple requirements checks, like +2 STR and +2 CON
                    "ability_boost DECIMAL(2,0) DEFAULT NULL, " +
                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "skill_proficiency VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(skill_proficiency = 'untrained' OR feature_proficiency = 'untrained')), " +

                    "CONSTRAINT archetype_feat_title PRIMARY KEY (feat_name, feat_level), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (ability_name, ability_boost) REFERENCES AbilityScore(ability_name, ability_boost), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    // -------------------- BASE DATA INSERTS (Rules) --------------------

    static String insertRuleSchemas =
            "INSERT INTO Size(size_type) VALUES " +
                    "('tiny'), ('small'), ('medium'), ('large'), ('huge'), ('gargantuan')" +
                    ";" +

                    "INSERT INTO AbilityBoost(ability_boost) VALUES" +
                    "(-4.0), (-3.0), (-2.0), (-1.0), (0.0), (1.0), (2.0), " +
                    "(3.0), (4.0), (4.5), (5.0), (5.5), (6.0), (6.5), " +
                    "(7.0), (7.5), (8.0), (8.5), (9.0), (9.5)" +
                    ";" +

                    "INSERT INTO Ability(ability_name) VALUES " +
                    "('Strength'), ('Dexterity'), ('Constitution'), " +
                    "('Intelligence'), ('Wisdom'), ('Charisma')" +
                    ";" +

                    "INSERT INTO AbilityScore(ability_name, ability_boost) " +
                    "SELECT abilities.ability_name, boosts.ability_boost " +
                    "FROM Ability abilities " +
                    "CROSS JOIN AbilityBoost boosts " +
                    "WHERE NOT EXISTS( " +
                    "SELECT * FROM AbilityScore ability_scores " +
                    "WHERE ability_scores.ability_name = abilities.ability_name " +
                    "AND ability_scores.ability_boost = boosts.ability_boost " +
                    ")" +
                    ";" +

                    "INSERT INTO Proficiency(proficiency_rank) VALUES " +
                    "('untrained'), ('trained'), ('expert'), ('master'), ('legendary')" +
                    ";" +

                    "INSERT INTO Skill(skill_name) VALUES" +
                    "('Acrobatics'), ('Arcana'), ('Athletics'), ('Crafting'), ('Deception'), " +
                    "('Diplomacy'), ('Intimidation'), ('Medicine'), ('Nature'), ('Occultism'), " +
                    "('Performance'), ('Religion'), ('Society'), ('Stealth'), ('Survival'), ('Thievery'), " +

                    "('Academia Lore'), ('Accounting Lore'), ('Architecture Lore'), ('Art Lore'), ('Astronomy Lore'), " +
                    "('Carpentry Lore'), ('Circus Lore'), ('Driving Lore'), ('Engineering Lore'), ('Farming Lore'), " +
                    "('Fishing Lore'), ('Fortune-Telling Lore'), ('Games Lore'), ('Genealogy Lore'), ('Gladiatorial Lore'), " +
                    "('Guild Lore'), ('Heraldry Lore'), ('Herbalism Lore'), ('Hunting Lore'), ('Labor Lore'), " +
                    "('Legal Lore'), ('Library Lore'), " +

                    "('Dwarfen Lore'), ('Elven Lore'), ('Goblin Lore'), ('Halfling Lore'), ('Leshy Lore'), ('Orc Lore'), " + //ancestries

                    "('Abadar Lore'), ('Iomedae Lore'), " + //deities

                    "('Demon Lore'), ('Giant Lore'), ('Vampire Lore'), " + //creatures

                    "('Astral Plane Lore'), ('Heaven Lore'), ('Outer Rifts Lore'), " + //planes

                    "('Hellknights Lore'), ('Pathfinder Society Lore'), " + //organizations

                    "('Absalom Lore'), ('Magnimar Lore'), " + //settlements

                    "('Mountain Lore'), ('River Lore'), " + //terrains

                    "('Alcohol Lore'), ('Baking Lore'), ('Butchering Lore'), ('Cooking Lore'), ('Tea Lore'), " + //items

                    "('Mercantile Lore'), ('Midwifery Lore'), ('Milling Lore'), " +
                    "('Mining Lore'), ('Piloting Lore'), ('Sailing Lore'), ('Scouting Lore'), ('Scribing Lore'), " +
                    "('Stabling Lore'), ('Tanning Lore'), ('Theater Lore'), ('Underworld Lore'), ('Warfare Lore')" +
                    ";" +

                    "INSERT INTO SkillRank(skill_name, proficiency_rank) " +
                    "SELECT skills.skill_name, proficiencies.proficiency_rank " +
                    "FROM Skill skills " +
                    "CROSS JOIN Proficiency proficiencies " +
                    "WHERE NOT EXISTS( " +
                    "SELECT * FROM SkillRank skill_ranks " +
                    "WHERE skill_ranks.skill_name = skills.skill_name " +
                    "AND skill_ranks.proficiency_rank = proficiencies.proficiency_rank " +
                    ")" +
                    ";" +

                    "INSERT INTO Feature(feature_name) VALUES" +
                    "('martial weapons'), ('simple weapons'), ('advanced weapons'), ('unarmed attacks'), ('spellcasting ability'), " +
                    "('unarmored defense'), ('light armor'), ('medium armor'), ('heavy armor'), " +
                    "('fortitude saves'), ('reflex saves'), ('will saves'), ('perception'), ('class dc')" +
                    ";" +

                    "INSERT INTO FeatureRank(feature_name, proficiency_rank) " +
                    "SELECT features.feature_name, proficiencies.proficiency_rank " +
                    "FROM Feature features " +
                    "CROSS JOIN Proficiency proficiencies " +
                    "WHERE NOT EXISTS(" +
                    "SELECT * FROM FeatureRank feature_ranks " +
                    "WHERE feature_ranks.feature_name = features.feature_name " +
                    "AND feature_ranks.proficiency_rank = proficiencies.proficiency_rank" +
                    ") " +
                    ";";

    // -------------------- CHARACTER CREATION INSERTS (Options) --------------------

    static String insertAncestries =
            "INSERT INTO Ancestry(ancestry_name, hit_points, size, speed, languages, vision) VALUES" +
                    "('Dwarf', 10, 'medium', 25, JSON_ARRAY('Common', 'Dwarven'), 'dark'), " +
                    "('Elf', 6, 'medium', 30, JSON_ARRAY('Common', 'Elven'), 'low-light'), " +
                    "('Gnome', 8, 'small', 25, JSON_ARRAY('Common', 'Fey', 'Gnomish'), 'low-light'), " +
                    "('Goblin', 6, 'small', 25, JSON_ARRAY('Common', 'Goblin'), 'dark'), " +
                    "('Halfling', 6, 'small', 25, JSON_ARRAY('Common', 'Halfling'), NULL), " +
                    "('Human', 8, 'medium', 25, JSON_ARRAY('Common'), NULL), " +
                    "('Leshy', 8, 'small', 25, JSON_ARRAY('Common', 'Fey'), 'low-light'), " +
                    "('Orc', 10, 'medium', 25, JSON_ARRAY('Common', 'Orcish'), 'dark')" +
                    ";" +
                    /*
                     * AncestryBoost
                     */
                    "INSERT INTO AncestryBoost(ancestry_name, ability_name, ability_flaw) VALUES" + //every class gets an additional free boost
                    "('Dwarf', 'Constitution', FALSE), " +
                    "('Dwarf', 'Wisdom', FALSE), " +
                    "('Dwarf', NULL, FALSE), " +
                    "('Dwarf', 'Charisma', TRUE), " +
                    "('Elf', 'Dexterity', FALSE), " +
                    "('Elf', 'Intelligence', FALSE), " +
                    "('Elf', NULL, FALSE), " +
                    "('Elf', 'Constitution', TRUE), " +
                    "('Gnome', 'Constitution', FALSE), " +
                    "('Gnome', 'Charisma', FALSE), " +
                    "('Gnome', NULL, FALSE), " +
                    "('Gnome', 'Strength', TRUE), " +
                    "('Goblin', 'Dexterity', FALSE), " +
                    "('Goblin', 'Charisma', FALSE), " +
                    "('Goblin', NULL, FALSE), " +
                    "('Goblin', 'Wisdom', TRUE), " +
                    "('Halfling', 'Dexterity', FALSE), " +
                    "('Halfling', 'Wisdom', FALSE), " +
                    "('Halfling', NULL, FALSE), " +
                    "('Halfling', 'Strength', TRUE), " +
                    "('Human', NULL, FALSE), " +
                    "('Human', NULL, FALSE), " +
                    "('Leshy', 'Constitution', FALSE), " +
                    "('Leshy', 'Wisdom', FALSE), " +
                    "('Leshy', NULL, FALSE), " +
                    "('Leshy', 'Intelligence', TRUE), " +
                    "('Orc', NULL, FALSE), " +
                    "('Orc', NULL, FALSE)" +
                    ";" +
                    /*
                     * AncestryHeritage
                     */
                    "INSERT INTO AncestryHeritage(ancestry_name, heritage_name, reference_title, skill_name, feature_name, feature_proficiency) VALUES" +
                    "('Orc', 'Badlands Orc', NULL, NULL, NULL, NULL), " +
                    "('Orc', 'Battle-Ready Orc', JSON_OBJECT('GeneralFeat','Intimidating Glare'), 'Intimidation', NULL, NULL), " +
                    "('Orc', 'Deep Orc', JSON_OBJECT('GeneralFeat', JSON_ARRAY('Terrain Expertise','Combat Climber')), NULL, NULL, NULL), " +
                    "('Orc', 'Grave Orc', NULL, NULL, NULL, NULL), " +
                    "('Orc', 'Hold-Scarred Orc', JSON_OBJECT('GeneralFeat','Diehard'), NULL, NULL, NULL), " + // 12 HP ancestry instead of 10
                    "('Orc', 'Rainfall Orc', NULL, NULL, NULL, NULL), " +
                    "('Orc', 'Winter Orc', NULL, 'Survival', NULL, NULL), " +
                    "('Leshy', 'Cactus Leshy', NULL, NULL, NULL, NULL), " +
                    "('Leshy', 'Fruit Leshy', NULL, NULL, NULL, NULL), " +
                    "('Leshy', 'Fungus Leshy', NULL, NULL, NULL, NULL), " +
                    "('Leshy', 'Gourd Leshy', NULL, NULL, NULL, NULL), " +
                    "('Leshy', 'Leaf Leshy', NULL, NULL, NULL, NULL), " +
                    "('Leshy', 'Lotus Leshy', NULL, NULL, NULL, NULL), " +
                    "('Leshy', 'Root Leshy', NULL, NULL, NULL, NULL), " +
                    "('Leshy', 'Seaweed Leshy', NULL, NULL, NULL, NULL), " +
                    "('Human', 'Skilled Human', JSON_OBJECT('AncestryFeat', 'General Training'), NULL, NULL, NULL), " +   // trained in one chosen skill (player choice -> NULL)
                    "('Human', 'Versatile Human', JSON_OBJECT('GeneralFeat', 'Skill Training'), NULL, NULL, NULL), " + // choose a general feat (player choice -> NULL)
                    "('Halfling', 'Gutsy Halfling', NULL, NULL, NULL, NULL), " +
                    "('Halfling', 'Hillock Halfling', NULL, NULL, NULL, NULL), " +
                    "('Halfling', 'Jinxed Halfling', NULL, NULL, NULL, NULL), " +
                    "('Halfling', 'Nomadic Halfling', NULL, NULL, NULL, NULL), " +
                    "('Halfling', 'Twilight Halfling', NULL, NULL, NULL, NULL), " +
                    "('Halfling', 'Wildwood Halfling', NULL, NULL, NULL, NULL), " +
                    "('Goblin', 'Charhide Goblin', NULL, NULL, NULL, NULL), " +
                    "('Goblin', 'Irongut Goblin', NULL, NULL, NULL, NULL), " +
                    "('Goblin', 'Razortooth Goblin', NULL, NULL, NULL, NULL), " +
                    "('Goblin', 'Snow Goblin', NULL, NULL, NULL, NULL), " +
                    "('Goblin', 'Unbreakable Goblin', NULL, NULL, NULL, NULL), " +
                    "('Gnome', 'Chameleon Gnome', NULL, NULL, NULL, NULL), " +
                    "('Gnome', 'Fey-touched Gnome', NULL, NULL, NULL, NULL), " +
                    "('Gnome', 'Sensate Gnome', NULL, NULL, NULL, NULL), " +
                    "('Gnome', 'Umbral Gnome', NULL, NULL, NULL, NULL), " +
                    "('Gnome', 'Wellspring Gnome', NULL, NULL, NULL, NULL), " +
                    "('Elf', 'Ancient Elf', NULL, NULL, NULL, NULL), " +
                    "('Elf', 'Arctic Elf', NULL, NULL, NULL, NULL), " +
                    "('Elf', 'Cavern Elf', NULL, NULL, NULL, NULL), " +
                    "('Elf', 'Seer Elf', NULL, NULL, NULL, NULL), " +
                    "('Elf', 'Whisper Elf', NULL, NULL, NULL, NULL), " +
                    "('Elf', 'Woodland Elf', NULL, NULL, NULL, NULL), " +
                    "('Dwarf', 'Ancient-Blooded Dwarf', NULL, NULL, NULL, NULL), " +
                    "('Dwarf', 'Death Warden Dwarf', NULL, NULL, NULL, NULL), " +
                    "('Dwarf', 'Forge Dwarf', NULL, NULL, NULL, NULL), " +
                    "('Dwarf', 'Rock Dwarf', NULL, NULL, NULL, NULL), " +
                    "('Dwarf', 'Strong-Blooded Dwarf', NULL, NULL, NULL, NULL)" +
                    ";";

    static String insertBackgrounds =
            "INSERT INTO Background(background_name, reference_title, lore_name) VALUES" +
                    "('Acolyte', JSON_OBJECT('GeneralFeat', 'Student of the Canon'), 'Scribing Lore'), " +
                    "('Acrobat', JSON_OBJECT('GeneralFeat', 'Steady Balance'), 'Circus Lore'), " +
                    "('Animal Whisperer', JSON_OBJECT('GeneralFeat', 'Train Animal'), NULL), " +
                    "('Artisan', JSON_OBJECT('GeneralFeat', 'Specialty Crafting'), 'Guild Lore'), " +
                    "('Artist', JSON_OBJECT('GeneralFeat', 'Specialty Crafting'), 'Art Lore'), " +
                    "('Bandit', JSON_OBJECT('GeneralFeat', 'Group Coercion'), NULL), " +
                    "('Barkeep', JSON_OBJECT('GeneralFeat', 'Hobnobber'), 'Alcohol Lore'), " +
                    "('Barrister', JSON_OBJECT('GeneralFeat', 'Group Impression'), 'Legal Lore'), " +
                    "('Bounty Hunter', JSON_OBJECT('GeneralFeat', 'Experienced Tracker'), 'Legal Lore'), " +
                    "('Charlatan', JSON_OBJECT('GeneralFeat', 'Charming Liar'), 'Underworld Lore'), " +
                    "('Cook', JSON_OBJECT('GeneralFeat', 'Seasoned'), 'Cooking Lore'), " +
                    "('Criminal', JSON_OBJECT('GeneralFeat', 'Experienced Smuggler'), 'Underworld Lore'), " +
                    "('Cultist', JSON_OBJECT('GeneralFeat', 'Schooled in Secrets'), 'Occultism'), " +
                    "('Detective', JSON_OBJECT('GeneralFeat', 'Streetwise'), 'Underworld Lore'), " +
                    "('Emissary', JSON_OBJECT('GeneralFeat', 'Multilingual'), NULL), " +
                    "('Entertainer', JSON_OBJECT('GeneralFeat', 'Fascinating Performance'), 'Theater Lore'), " +
                    "('Farmhand', JSON_OBJECT('GeneralFeat', 'Assurance'), 'Farming Lore'), " +
                    "('Field Medic', JSON_OBJECT('GeneralFeat', 'Battle Medicine'), 'Medicine'), " +
                    "('Fortune Teller', JSON_OBJECT('GeneralFeat', 'Oddity Identification'), 'Fortune-Telling Lore'), " +
                    "('Gambler', JSON_OBJECT('GeneralFeat', 'Lie to Me'), 'Games Lore'), " +
                    "('Gladiator', JSON_OBJECT('GeneralFeat', 'Impressive Performance'), 'Gladiatorial Lore'), " +
                    "('Guard', JSON_OBJECT('GeneralFeat', 'Quick Coercion'), NULL), " +
                    "('Herbalist', JSON_OBJECT('GeneralFeat', 'Natural Medicine'), 'Herbalism Lore'), " +
                    "('Hermit', JSON_OBJECT('GeneralFeat', 'Dubious Knowledge'), NULL), " +
                    "('Hunter', JSON_OBJECT('GeneralFeat', 'Survey Wildlife'), 'Tanning Lore'), " +
                    "('Laborer', JSON_OBJECT('GeneralFeat', 'Hefty Hauler'), 'Labor Lore'), " +
                    "('Martial Disciple', JSON_OBJECT('GeneralFeat', 'Cat Fall, Quick Jump'), NULL), " +
                    "('Merchant', JSON_OBJECT('GeneralFeat', 'Bargain Hunter'), 'Mercantile Lore'), " +
                    "('Miner', JSON_OBJECT('GeneralFeat', 'Terrain Expertise'), 'Mining Lore'), " +
                    "('Noble', JSON_OBJECT('GeneralFeat', 'Courtly Graces'), NULL), " +
                    "('Nomad', JSON_OBJECT('GeneralFeat', 'Assurance'), NULL), " +
                    "('Prisoner', JSON_OBJECT('GeneralFeat', 'Experienced Smuggler'), 'Underworld Lore'), " +
                    "('Raised by Belief', JSON_OBJECT('GeneralFeat', 'Assurance'), NULL), " +
                    "('Sailor', JSON_OBJECT('GeneralFeat', 'Underwater Marauder'), 'Sailing Lore'), " +
                    "('Scholar', JSON_OBJECT('GeneralFeat', 'Assurance'), 'Academia Lore'), " +
                    "('Scout', JSON_OBJECT('GeneralFeat', 'Forager'), NULL), " +
                    "('Street Urchin', JSON_OBJECT('GeneralFeat', 'Pickpocket'), NULL), " +
                    "('Teacher', JSON_OBJECT('GeneralFeat', 'Experienced Professional'), 'Academia Lore'), " +
                    "('Tinker', JSON_OBJECT('GeneralFeat', 'Specialty Crafting'), 'Engineering Lore'), " +
                    "('Warrior', JSON_OBJECT('GeneralFeat', 'Intimidating Glare'), 'Warfare Lore') " +
                    ";" +
                    /*
                     * BackgroundSkill
                     */
                    "INSERT INTO BackgroundSkill(background_name, skill_name, additional_skill) VALUES" +
                    "('Acolyte', 'Religion', FALSE), " +
                    "('Acrobat', 'Acrobatics', FALSE), " +
                    "('Animal Whisperer', 'Nature', FALSE), " +
                    "('Artisan', 'Crafting', FALSE), " +
                    "('Artist', 'Crafting', FALSE), " +
                    "('Bandit', 'Intimidation', FALSE), " +
                    "('Barkeep', 'Diplomacy', FALSE), " +
                    "('Barrister', 'Diplomacy', FALSE), " +
                    "('Bounty Hunter', 'Survival', FALSE), " +
                    "('Charlatan', 'Deception', FALSE), " +
                    "('Cook', 'Survival', FALSE), " +
                    "('Criminal', 'Stealth', FALSE), " +
                    "('Cultist', 'Occultism', FALSE), " +
                    "('Detective', 'Society', FALSE), " +
                    "('Emissary', 'Society', FALSE), " +
                    "('Entertainer', 'Performance', FALSE), " +
                    "('Farmhand', 'Athletics', FALSE), " +
                    "('Field Medic', 'Medicine', FALSE), " +
                    "('Fortune Teller', 'Occultism', FALSE), " +
                    "('Gambler', 'Deception', FALSE), " +
                    "('Gladiator', 'Performance', FALSE), " +
                    "('Guard', 'Intimidation', FALSE), " +
                    "('Herbalist', 'Nature', FALSE), " +
                    "('Hermit', 'Nature', FALSE), " +
                    "('Hermit', 'Occultism', FALSE), " +
                    "('Hunter', 'Survival', FALSE), " +
                    "('Laborer', 'Athletics', FALSE), " +
                    "('Martial Disciple', 'Acrobatics', FALSE), " +
                    "('Martial Disciple', 'Athletics', FALSE), " +
                    "('Merchant', 'Diplomacy', FALSE), " +
                    "('Miner', 'Survival', FALSE), " +
                    "('Noble', 'Society', FALSE), " +
                    "('Nomad', 'Survival', FALSE), " +
                    "('Prisoner', 'Stealth', FALSE), " +
                    "('Raised by Belief', NULL, FALSE), " +
                    "('Sailor', 'Athletics', FALSE), " +
                    "('Scholar', NULL, FALSE), " +
                    "('Scout', 'Survival', FALSE), " +
                    "('Street Urchin', 'Thievery', FALSE), " +
                    "('Teacher', 'Performance', FALSE), " +
                    "('Teacher', 'Society', FALSE), " +
                    "('Tinker', 'Crafting', FALSE), " +
                    "('Warrior', 'Intimidation', FALSE) " +
                    ";" +
                    /*
                     * BackgroundBoost
                     */
                    "INSERT INTO BackgroundBoost(background_name, ability_boost) VALUES" + //every background gets an additional free boost
                    "('Acolyte', 'Intelligence'), ('Acolyte', 'Wisdom'), " +
                    "('Acrobat', 'Strength'), ('Acrobat', 'Dexterity'), " +
                    "('Animal Whisperer', 'Wisdom'), ('Animal Whisperer', 'Charisma'), " +
                    "('Artisan', 'Strength'), ('Artisan', 'Intelligence'), " +
                    "('Artist', 'Dexterity'), ('Artist', 'Charisma'), " +
                    "('Bandit', 'Dexterity'), ('Bandit', 'Charisma'), " +
                    "('Barkeep', 'Constitution'), ('Barkeep', 'Charisma'), " +
                    "('Barrister', 'Intelligence'), ('Barrister', 'Charisma'), " +
                    "('Bounty Hunter', 'Strength'), ('Bounty Hunter', 'Wisdom'), " +
                    "('Charlatan', 'Intelligence'), ('Charlatan', 'Charisma'), " +
                    "('Cook', 'Constitution'), ('Cook', 'Intelligence'), " +
                    "('Criminal', 'Dexterity'), ('Criminal', 'Intelligence'), " +
                    "('Cultist', 'Intelligence'), ('Cultist', 'Charisma'), " +
                    "('Detective', 'Intelligence'), ('Detective', 'Wisdom'), " +
                    "('Emissary', 'Intelligence'), ('Emissary', 'Charisma'), " +
                    "('Entertainer', 'Dexterity'), ('Entertainer', 'Charisma'), " +
                    "('Farmhand', 'Constitution'), ('Farmhand', 'Wisdom'), " +
                    "('Field Medic', 'Constitution'), ('Field Medic', 'Wisdom'), " +
                    "('Fortune Teller', 'Intelligence'), ('Fortune Teller', 'Charisma'), " +
                    "('Gambler', 'Dexterity'), ('Gambler', 'Charisma'), " +
                    "('Gladiator', 'Strength'), ('Gladiator', 'Charisma'), " +
                    "('Guard', 'Strength'), ('Guard', 'Charisma'), " +
                    "('Herbalist', 'Constitution'), ('Herbalist', 'Wisdom'), " +
                    "('Hermit', 'Constitution'), ('Hermit', 'Intelligence'), " +
                    "('Hunter', 'Dexterity'), ('Hunter', 'Wisdom'), " +
                    "('Laborer', 'Strength'), ('Laborer', 'Constitution'), " +
                    "('Martial Disciple', 'Strength'), ('Martial Disciple', 'Dexterity'), " +
                    "('Merchant', 'Intelligence'), ('Merchant', 'Charisma'), " +
                    "('Miner', 'Strength'), ('Miner', 'Wisdom'), " +
                    "('Noble', 'Intelligence'), ('Noble', 'Charisma'), " +
                    "('Nomad', 'Constitution'), ('Nomad', 'Wisdom'), " +
                    "('Prisoner', 'Strength'), ('Prisoner', 'Constitution'), " +
                    "('Raised by Belief', NULL), " +
                    "('Sailor', 'Strength'), ('Sailor', 'Dexterity'), " +
                    "('Scholar', 'Intelligence'), ('Scholar', 'Wisdom'), " +
                    "('Scout', 'Dexterity'), ('Scout', 'Wisdom'), " +
                    "('Street Urchin', 'Dexterity'), ('Street Urchin', 'Constitution'), " +
                    "('Teacher', 'Intelligence'), ('Teacher', 'Wisdom'), " +
                    "('Tinker', 'Dexterity'), ('Tinker', 'Intelligence'), " +
                    "('Warrior', 'Strength'), ('Warrior', 'Constitution') " +
                    ";";

    static String insertClasses =
            "INSERT INTO Class(class_name, key_ability, secondary_ability, hit_points, additional_skills) VALUES" +
                    "('Bard', 'Charisma', NULL, 8, 4), " +
                    "('Cleric', 'Wisdom', NULL, 8, 2), " +
                    "('Druid', 'Wisdom', NULL, 8, 2), " +
                    "('Fighter', 'Strength', 'Dexterity', 10, 3), " +
                    "('Ranger', 'Dexterity', 'Strength', 10, 4), " +
                    "('Rogue', 'Dexterity', NULL, 8, 7), " + //Rogue other ability score is special feature (using AlterTable)
                    "('Witch', 'Intelligence', NULL, 6, 3), " +
                    "('Wizard', 'Intelligence', NULL, 6, 2), " +
                    "('Barbarian', 'Strength', NULL, 12, 3)" +
                    "; " +
                    /*
                     * ClassProficiency
                     */
                    "INSERT INTO ClassProficiency(class_name, feature_name, feature_proficiency) VALUES " +
                    "('Fighter', 'perception', 'expert'), " +
                    "('Fighter', 'spellcasting ability', DEFAULT), " +
                    "('Fighter', 'class dc', 'trained'), " +
                    "('Fighter', 'unarmored defense', 'trained'), " +
                    "('Fighter', 'light armor', 'trained'), " +
                    "('Fighter', 'medium armor', 'trained'), " +
                    "('Fighter', 'heavy armor', 'trained'), " +
                    "('Fighter', 'unarmed attacks', 'expert'), " +
                    "('Fighter', 'simple weapons', 'expert'), " +
                    "('Fighter', 'martial weapons', 'expert'), " +
                    "('Fighter', 'advanced weapons', 'trained'), " +
                    "('Fighter', 'fortitude saves', 'expert'), " +
                    "('Fighter', 'reflex saves', 'expert'), " +
                    "('Fighter', 'will saves', 'trained')" +
                    ";" +
                    /*
                     * ClassSkill
                     */
                    "INSERT INTO ClassSkill(class_name, skill_name, additional_skill) VALUES" +
                    "('Bard', 'Occultism', FALSE), " +
                    "('Bard', 'Performance', TRUE), " +
                    "('Cleric', 'Religion', FALSE), " +
                    "('Druid', 'Nature', FALSE), " +
                    "('Fighter', 'Acrobatics', FALSE), " +
                    "('Fighter', 'Athletics', FALSE), " +
                    "('Ranger', 'Nature', FALSE), " +
                    "('Ranger', 'Survival', TRUE), " +
                    "('Rogue', 'Stealth', FALSE), " +
                    "('Wizard', 'Arcana', FALSE), " +
                    "('Barbarian', 'Athletics', FALSE) " +
                    ";" +
                    /*
                     * ClassOption
                     */
                    "INSERT INTO ClassOption(class_name, option_level, class_option, reference_title, trained_skills, feature_name, feature_proficiency) VALUES" +
                    "('Bard', 1, 'Enigma', NULL, NULL, NULL, NULL), " +
                    "('Bard', 1, 'Maestro', NULL, NULL, NULL, NULL), " +
                    "('Bard', 1, 'Polymath', NULL, NULL, NULL, NULL), " +
                    "('Bard', 1, 'Warrior', NULL, NULL, NULL, NULL), " +
                    "('Cleric', 1, 'Cloistered Cleric', JSON_OBJECT('Class Feat', 'Domain Initiate'), NULL, NULL, NULL), " +
                    "('Cleric', 1, 'Warpriest', JSON_OBJECT('GeneralFeat', 'Shield Block', 'ClassFeat', 'Deadly Simplicity'), NULL, 'medium armor', 'trained'), " + //expert at 13th level
                    "('Cleric', 1, 'Warpriest', JSON_OBJECT('GeneralFeat', 'Shield Block', 'ClassFeat', 'Deadly Simplicity'), NULL, 'light armor', 'trained'), " + //expert at 13th level
                    "('Cleric', 1, 'Warpriest', JSON_OBJECT('GeneralFeat', 'Shield Block', 'ClassFeat', 'Deadly Simplicity'), NULL, 'fortitude saves', 'expert'), " + //master at 15th level
                    "('Cleric', 1, 'Healing Font', NULL, NULL, NULL, NULL), " +
                    "('Cleric', 1, 'Harmful Font', NULL, NULL, NULL, NULL), " +
                    "('Druid', 1, 'Animal', JSON_OBJECT('Spell','Heal Animal', 'ClassFeat', 'Animal Companion'), JSON_ARRAY('Athletics'), NULL, NULL), " +
                    "('Druid', 1, 'Leaf', JSON_OBJECT('Spell','Cornucopia', 'ClassFeat', 'Leshy Familiar'), JSON_ARRAY('Diplomacy'), NULL, NULL), " +
                    "('Druid', 1, 'Storm', JSON_OBJECT('Spell','Tempest Surge', 'ClassFeat', 'Storm Born'), JSON_ARRAY('Acrobatics'), NULL, NULL), " +
                    "('Druid', 1, 'Untamed', JSON_OBJECT('Spell','Untamed Shift', 'ClassFeat', 'Untamed Form'), JSON_ARRAY('Intimidation'), NULL, NULL), " +
                    "('Druid', 1, NULL, JSON_OBJECT('GeneralFeat', 'Shield Block'), NULL, NULL, NULL), " +
                    "('Fighter', 1, NULL, JSON_OBJECT('GeneralFeat', 'Shield Block', 'ClassFeat', 'Reactive Strike'), NULL, NULL, NULL), " + //adds Shield Block and Reactive Strike for Level 1 Fighter
                    "('Ranger', 1, 'Flurry', NULL, NULL, NULL, NULL), " +
                    "('Ranger', 1, 'Outwit', NULL, NULL, NULL, NULL), " +
                    "('Ranger', 1, 'Precision', NULL, NULL, NULL, NULL), " +
                    "('Rogue', 1, 'Mastermind', NULL, JSON_ARRAY('Society', NULL), NULL, NULL), " +
                    "('Rogue', 1, 'Ruffian', NULL, JSON_ARRAY('Intimidation'), NULL, NULL), " +
                    "('Rogue', 1, 'Scoundrel', NULL, JSON_ARRAY('Deception', 'Diplomacy'), NULL, NULL), " +
                    "('Rogue', 1, 'Thief', NULL, JSON_ARRAY('Thievery'), NULL, NULL), " +
                    "('Witch', 1, 'Faith’s Flamekeeper', JSON_OBJECT('Tradition', 'Divine'), JSON_ARRAY('Religion'), NULL, NULL), " +
                    "('Witch', 1, 'The Inscribed One', JSON_OBJECT('Tradition', 'Arcane'), JSON_ARRAY('Arcana'), NULL, NULL), " +
                    "('Witch', 1, 'The Resentment', JSON_OBJECT('Tradition', 'Occult'), JSON_ARRAY('Occultism'), NULL, NULL), " +
                    "('Witch', 1, 'Silence in Snow', JSON_OBJECT('Tradition', 'Primal'), JSON_ARRAY('Nature'), NULL, NULL), " +
                    "('Witch', 1, 'Spinner of Threads', JSON_OBJECT('Tradition', 'Occult'), JSON_ARRAY('Occultism'), NULL, NULL), " +
                    "('Witch', 1, 'Starless Shadow', JSON_OBJECT('Tradition', 'Occult'), JSON_ARRAY('Occultism'), NULL, NULL), " +
                    "('Witch', 1, 'Wilding Steward', JSON_OBJECT('Tradition', 'Primal'), JSON_ARRAY('Nature'), NULL, NULL), " +
                    "('Wizard', 1, 'Experimental Spellshaping', JSON_OBJECT('ClassFeat', NULL, 'Traits', 'Spellshape'), NULL, NULL, NULL), " +
                    "('Wizard', 1, 'Improved Familiar Attunement', JSON_OBJECT('ClassFeat', 'Familiar'), NULL, NULL, NULL), " +
                    "('Wizard', 1, 'Spell Blending', NULL, NULL, NULL, NULL), " +
                    "('Wizard', 1, 'Spell Substitution', NULL, NULL, NULL, NULL), " +
                    "('Wizard', 1, 'Staff Nexus', NULL, NULL, NULL, NULL) " +
                    ";";

    static String insertFeats =
            /*
             * AncestryFeats
             */
            "INSERT INTO AncestryFeat(feat_name, feat_level, ancestry_name, ability_name, ability_boost, skill_name, skill_proficiency, feature_name, feature_proficiency) VALUES" +
                    "('Adapted Cantrip', 1, 'Human',  NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Ancestral Longevity', 1, 'Elf',  NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Animal Accomplice', 1, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Animal Elocutionist', 1, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Beast Trainer', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Burn It!', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('City Scavenger', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Cooperative Nature', 1, 'Human',  NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Distracting Shadows', 1, 'Halfling',NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Dwarven Doughtiness', 1, 'Dwarf',  NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Dwarven Lore', 1, 'Dwarf',  NULL, NULL, NULL, NULL, NULL, NULL), " + //-- player picks Dwarven Lore; proficiency left NULL
                    "('Dwarven Weapon Familiarity', 1, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Elven Lore', 1, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL), " + //-- player picks Elven Lore; proficiency left NULL"
                    "('Elven Weapon Familiarity', 1, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Fey Fellowship', 1, 'Gnome',  NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('First World Magic', 1, 'Gnome',  NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Folksy Patter', 1, 'Halfling',NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Forlorn', 1, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('General Training', 1, 'Human', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Gnome Obsession', 1, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Gnome Weapon Familiarity', 1, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Goblin Lore', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL), " + // -- player picks Goblin Lore; proficiency left NULL
                    "('Goblin Scuttle', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Goblin Song', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Goblin Weapon Familiarity', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Grasping Reach', 1, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Halfling Lore', 1, 'Halfling',NULL, NULL, NULL, NULL, NULL, NULL), " + //-- player picks Halfling Lore; proficiency left NULL
                    "('Halfling Luck', 1, 'Halfling',NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Halfling Weapon Familiarity', 1, 'Halfling',NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Harmlessly Cute', 1, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Haughty Obstinacy', 1, 'Human', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Hold Mark', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Illusion Sense', 1, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Iron Fists', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Junk Tinker', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Leshy Lore', 1, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL), " + //-- player picks Leshy Lore; proficiency left NULL
                    "('Leshy Superstition', 1, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Mountain Strategy', 1, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Natural Ambition', 1, 'Human', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Natural Skill', 1, 'Human', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Nimble Elf', 1, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Orc Ferocity', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Orc Lore', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL), " + //-- player picks Orc Lore; proficiency left NULL
                    "('Orc Superstition', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Orc Weapon Familiarity', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Otherworldly Magic', 1, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Prairie Rider', 1, 'Halfling',NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Razzle-Dazzle', 1, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Rock Runner', 1, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Rough Rider', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Seedpod', 1, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Shadow of the Wilds', 1, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Stonemasons Eye', 1, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Sure Feet', 1, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Titan Slinger', 1, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Tusks', 1, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unburdened Iron', 1, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unconventional Weaponry', 1, 'Human', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Undaunted', 1, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unfettered Halfling', 1, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unwavering Mien', 1, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Very Sneaky', 1, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Watchful Halfling', 1, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL) " +
                    ";" +
                    /*
                     * GeneralFeats and SkillFeats; [WIP] Bad Constraints for ability_name/skill_name
                     */
                    "INSERT INTO GeneralFeat(feat_name, feat_level, ability_name, ability_boost, skill_name, skill_proficiency, feature_name, feature_proficiency) VALUES" +
                    "('Additional Lore', 1, NULL, NULL, NULL, NULL, NULL, NULL), " + //player picks a Lore; proficiency left NULL
                    "('Adopted Ancestry', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    //"('Alchemical Crafting', 1, NULL, NULL, 'Crafting', 'trained', NULL, NULL)," +
                    //"('Arcane Sense', 1, NULL, NULL, 'Arcana', 'trained', NULL, NULL)," +
                    "('Armor Proficiency', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Assurance', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    //"('Bargain Hunter', 1, NULL, NULL, 'Diplomacy', 'trained', NULL, NULL)," +
                    //"('Battle Medicine', 1, NULL, NULL, 'Medicine', 'trained', NULL, NULL)," +
                    "('Breath Control', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Canny Acumen', 1, NULL, NULL, NULL, NULL, 'Perception', 'expert')," +
//                    "('Cat Fall', 1, NULL, NULL, 'Acrobatics', 'trained', NULL, NULL)," +
//                    "('Charming Liar', 1, NULL, NULL, 'Deception', 'trained', NULL, NULL)," +
//                    "('Combat Climber', 1, NULL, NULL, 'Athletics', 'trained', NULL, NULL)," +
//                    "('Courtly Graces', 1, NULL, NULL, 'Society', 'trained', NULL, NULL)," +
                    "('Diehard', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Dubious Knowledge', 1, NULL, NULL, NULL, NULL, NULL, NULL), " + //skill_name NULL; proficiency removed
//                    "('Experienced Professional', 1, NULL, NULL, NULL, NULL, NULL, NULL), " + //player picks a Lore
//                    "('Experienced Smuggler', 1, NULL, NULL, 'Stealth', 'trained', NULL, NULL)," +
//                    "('Experienced Tracker', 1, NULL, NULL, 'Survival', 'trained', NULL, NULL)," +
//                    "('Fascinating Performance', 1, NULL, NULL, 'Performance', 'trained', NULL, NULL)," +
//                    "('Fast Recovery', 1, 'Constitution', 2.0, NULL, NULL, NULL, NULL)," +
//                    "('Feather Step', 1, 'Dexterity', 2.0, NULL, NULL, NULL, NULL)," +
//                    "('Fleet', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
//                    "('Forager', 1, NULL, NULL, 'Survival', 'trained', NULL, NULL)," +
//                    "('Group Coercion', 1, NULL, NULL, 'Intimidation', 'trained', NULL, NULL)," +
//                    "('Group Impression', 1, NULL, NULL, 'Diplomacy', 'trained', NULL, NULL)," +
//                    "('Hefty Hauler', 1, NULL, NULL, 'Athletics', 'trained', NULL, NULL)," +
//                    "('Hobnobber', 1, NULL, NULL, 'Diplomacy', 'trained', NULL, NULL)," +
//                    "('Impressive Performance', 1, NULL, NULL, 'Performance', 'trained', NULL, NULL)," +
                    "('Incredible Initiative', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
//                    "('Intimidating Glare', 1, NULL, NULL, 'Intimidation', 'trained', NULL, NULL)," +
//                    "('Lengthy Diversion', 1, NULL, NULL, 'Deception', 'trained', NULL, NULL)," +
//                    "('Lie to Me', 1, NULL, NULL, 'Deception', 'trained', NULL, NULL)," +
//                    "('Multilingual', 1, NULL, NULL, 'Society', 'trained', NULL, NULL)," +
//                    "('Natural Medicine', 1, NULL, NULL, 'Nature', 'trained', NULL, NULL)," +
//                    "('No Cause for Alarm', 1, NULL, NULL, 'Diplomacy', 'trained', NULL, NULL)," +
//                    "('Oddity Identification', 1, NULL, NULL, 'Occultism', 'trained', NULL, NULL)," +
                    "('Pet', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
//                    "('Pickpocket', 1, NULL, NULL, 'Thievery', 'trained', NULL, NULL)," +
//                    "('Quick Coercion', 1, NULL, NULL, 'Intimidation', 'trained', NULL, NULL)," +
//                    "('Quick Identification', 1, NULL, NULL, NULL, NULL, NULL, NULL), " + //skill_name NULL; proficiency removed
//                    "('Quick Jump', 1, NULL, NULL, 'Athletics', 'trained', NULL, NULL)," +
//                    "('Quick Repair', 1, NULL, NULL, 'Crafting', 'trained', NULL, NULL)," +
//                    "('Quick Squeeze', 1, NULL, NULL, 'Acrobatics', 'trained', NULL, NULL)," +
//                    "('Read Lips', 1, NULL, NULL, 'Society', 'trained', NULL, NULL)," +
                    "('Recognize Spell', 1, NULL, NULL, NULL, NULL, NULL, NULL), " + //skill_name NULL; proficiency removed
                    "('Ride', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
//                    "('Schooled in Secrets', 1, NULL, NULL, 'Occultism', 'trained', NULL, NULL)," +
                    "('Seasoned', 1, NULL, NULL, NULL, NULL, NULL, NULL), " + //skill_name NULL; proficiency removed
                    "('Shield Block', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
//                    "('Sign Language', 1, NULL, NULL, 'Society', 'trained', NULL, NULL)," +
                    //"('Skill Training', 1, 'Intelligence', 1.0, NULL, NULL, NULL, NULL)," +
//                    "('Specialty Crafting', 1, NULL, NULL, 'Crafting', 'trained', NULL, NULL)," +
//                    "('Steady Balance', 1, NULL, NULL, 'Acrobatics', 'trained', NULL, NULL)," +
//                    "('Streetwise', 1, NULL, NULL, 'Society', 'trained', NULL, NULL)," +
//                    "('Student of the Canon', 1, NULL, NULL, 'Religion', 'trained', NULL, NULL)," +
//                    "('Subtle Theft', 1, NULL, NULL, 'Thievery', 'trained', NULL, NULL)," +
//                    "('Survey Wildlife', 1, NULL, NULL, 'Survival', 'trained', NULL, NULL)," +
//                    "('Terrain Expertise', 1, NULL, NULL, 'Survival', 'trained', NULL, NULL)," +
//                    "('Terrain Stalker', 1, NULL, NULL, 'Stealth', 'trained', NULL, NULL)," +
//                    "('Titan Wrestler', 1, NULL, NULL, 'Athletics', 'trained', NULL, NULL)," +
                    "('Toughness', 1, NULL, NULL, NULL, NULL, NULL, NULL)," +
//                    "('Train Animal', 1, NULL, NULL, 'Nature', 'trained', NULL, NULL)," +
                    "('Trick Magic Item', 1, NULL, NULL, NULL, NULL, NULL, NULL), " + //skill_name NULL; proficiency removed"
//                    "('Underwater Marauder', 1, NULL, NULL, 'Athletics', 'trained', NULL, NULL)," +
//                    "('Virtuosic Performer', 1, NULL, NULL, 'Performance', 'trained', NULL, NULL)," +
                    "('Weapon Proficiency', 1, NULL, NULL, NULL, NULL, NULL, NULL)" +
                    ";" +
                    "INSERT INTO SkillFeat(feat_name, feat_level, skill_name, skill_proficiency, ability_name, ability_boost, feature_name, feature_proficiency) " +
                    "SELECT feat_name, feat_level, skill_name, skill_proficiency, ability_name, ability_boost, feature_name, feature_proficiency " +
                    "FROM GeneralFeat " +
                    "WHERE skill_name IS NOT NULL" +
                    ";" +
                    /*
                     * ClassFeats
                     */
                    "INSERT INTO ClassFeat(feat_name, feat_level, class_name, ability_name, ability_boost, skill_name, skill_proficiency, feature_name, feature_proficiency) VALUES " +
                    "('Aggressive Block', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Brutish Shove', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Combat Assessment', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Double Slice', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Exacting Strike', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Point-Blank Stance', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Reactive Shield', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Snagging Strike', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Sudden Charge', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Twin Parry', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Vicious Swing', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Felling Strike', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Quick Reversal', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Swipe', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Assisting Shot', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Blade Break', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Combat Grab', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Disarming Stance', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Dueling Parry', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Intimidating Strike', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Lightning Swap', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Lunge', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Rebounding Toss', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Sleek Reposition', 2, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Powerful Shove', 4, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Slam Down', 4, 'Fighter', NULL, NULL, 'Athletics', 'trained', NULL, NULL) " +
                    ";";
    static String insertActions =
            "INSERT INTO Action(action_name, action_level, frequency, action, `trigger`, requirement) VALUES " +
                    "('Aggressive Block', 1, NULL, 'reaction', 'You use the Shield Block reaction, and the opponent that triggered Shield Block is adjacent to you and is your size or smaller', NULL), " +
                    "('Brutish Shove', 1, NULL, 'one-action', NULL, NULL), " +
                    "('Combat Assessment', 1, NULL, 'one-action', NULL, NULL), " +
                    "('Double Slice', 1, NULL, 'two-action', NULL, 'You are wielding two melee weapons, each in a different hand'), " +
                    "('Exacting Strike', 1, NULL, 'one-action', NULL, NULL), " +
                    "('Point-Blank Stance', 1, NULL, 'one-action', NULL, 'You are wielding a ranged weapon'), " +
                    "('Reactive Shield', 1, NULL, 'reaction', 'An enemy hits you with a melee Strike', 'You are wielding a shield'), " +
                    "('Snagging Strike', 1, NULL, 'one-action', NULL, 'You have one hand free, and your target is within reach of that hand'), " +
                    "('Sudden Charge', 1, NULL, 'two-action', NULL, NULL), " +
                    "('Twin Parry', 1, NULL, 'one-action', NULL, 'You are wielding two melee weapons, one in each hand'), " +
                    "('Vicious Swing', 1, NULL, 'two-action', NULL, NULL), " +
                    "('Felling Strike', 2, NULL, 'two-action', NULL, NULL), " +
                    "('Quick Reversal', 2, NULL, 'one-action', NULL, NULL), " +
                    "('Swipe', 2, NULL, 'two-action', NULL, NULL), " +
                    "('Assisting Shot', 2, NULL, 'one-action', NULL, NULL), " +
                    "('Blade Break', 2, NULL, 'reaction', NULL, NULL), " +
                    "('Combat Grab', 2, NULL, 'one-action', NULL, 'You must Strike with a free hand'), " +
                    "('Disarming Stance', 2, NULL, 'one-action', NULL, NULL), " +
                    "('Dueling Parry', 2, NULL, 'one-action', NULL, 'You wield a one-handed weapon'), " +
                    "('Intimidating Strike', 2, NULL, 'one-action', NULL, NULL), " +
                    "('Lightning Swap', 2, NULL, 'one-action', NULL, NULL), " +
                    "('Lunge', 2, NULL, 'one-action', NULL, NULL), " +
                    "('Rebounding Toss', 2, NULL, 'two-action', NULL, NULL), " +
                    "('Sleek Reposition', 2, NULL, 'free-action', NULL, NULL), " +
                    "('Slam Down', 4, NULL, 'two-action', NULL, NULL) " +
                    ";";
    static String insertSources =
            /*Source for Feats, need to source the ancestry/general feats; the ancestries, classes, and background*/
            "INSERT INTO Source(name, level, book, page, description, rarity, traits, prerequisites) VALUES" +
                    "('Aggressive Block', 1, 'Player Core', 144, 'When you block, you also push the enemy away.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Brutish Shove', 1, 'Player Core', 145, 'Use your brute strength to Shove as part of an attack.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Combat Assessment', 1, 'Player Core', 141, 'Make a Strike and immediately attempt Recall Knowledge about the target as a free action.', DEFAULT, JSON_ARRAY('Fighter','Flourish'), NULL), " +
                    "('Double Slice', 1, 'Player Core', 141, 'Strike with each of two melee weapons, both requiring the same target.', DEFAULT, JSON_ARRAY('Fighter','Flourish','Open'), NULL), " +
                    "('Exacting Strike', 1, 'Player Core', 142, 'Make a Strike with precision to reduce penalties if you miss.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Point-Blank Stance', 1, 'Player Core', 142, 'Enter stance to improve accuracy and damage with ranged weapons within 30 feet.', DEFAULT, JSON_ARRAY('Fighter','Stance'), NULL), " +
                    "('Reactive Shield', 1, 'Player Core', 142, 'Raise a Shield as a reaction when an enemy hits you.', DEFAULT, JSON_ARRAY('Fighter','Reaction'), NULL), " +
                    "('Snagging Strike', 1, 'Player Core', 142, 'Strike and grab your foe’s weapon arm to interfere with its attacks.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Sudden Charge', 1, 'Player Core', 143, 'Stride twice and make a Strike.', DEFAULT, JSON_ARRAY('Fighter','Flourish'), NULL), " +
                    "('Twin Parry', 1, 'Player Core', 146, 'Parry with a weapon in each hand to gain a defensive boost.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Vicious Swing', 1, 'Player Core', 143, 'Make a powerful swing that deals extra damage but increases your multiple attack penalty.', DEFAULT, JSON_ARRAY('Fighter','Flourish'), NULL), " +
                    "('Felling Strike', 2, 'Player Core', 144, 'A sweeping attack that can topple large foes.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Quick Reversal', 2, 'Player Core', 144, 'Exploit a foe’s miss to attack another creature.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Swipe', 2, 'Player Core', 144, 'Attack two foes with one swing.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Assisting Shot', 2, 'Player Core', 144, 'Shoot to aid an ally’s attack.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Blade Break', 2, 'Player Core', 145, 'Attempt to parry or damage an opponent’s weapon.', DEFAULT, JSON_ARRAY('Fighter','Reaction'), NULL), " +
                    "('Combat Grab', 2, 'Player Core', 146, 'Strike and grab your foe in one motion.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Disarming Stance', 2, 'Player Core', 146, 'Enter stance to gain bonuses on Disarm attempts.', DEFAULT, JSON_ARRAY('Fighter','Stance'), NULL), " +
                    "('Dueling Parry', 2, 'Player Core', 146, 'Parry with a one‑handed weapon for AC bonus.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Intimidating Strike', 2, 'Player Core', 147, 'Strike to damage and attempt to Demoralize.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Lightning Swap', 2, 'Player Core', 147, 'Swap weapons and Strike as part of one action.', DEFAULT, JSON_ARRAY('Fighter','Flourish'), NULL), " +
                    "('Lunge', 2, 'Player Core', 147, 'Extend your reach for a single Strike.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Rebounding Toss', 2, 'Player Core', 148, 'Throw a weapon to rebound and strike again.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Sleek Reposition', 2, 'Player Core', 148, 'Move into perfect position after a Strike.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL)," +
                    "('Powerful Shove', 4, 'Player Core', 143, 'You can push larger foes around with your attack. You can use Aggressive Block or Brutish Shove against a creature up to two sizes larger than you.\nWhen a creature you Shove or knock back with a shield, polearm, or club’s critical specialization effect has to stop moving because it would hit an object, it takes damage equal to your Strength modifier (minimum 1).', DEFAULT, JSON_ARRAY('Fighter', 'Press'), JSON_OBJECT('ClassFeat', JSON_ARRAY('Aggressive Block', 'Brutish Shove')))," +
                    "('Slam Down', 4, 'Player Core', 143, 'You make an attack to knock a foe off balance, then follow up immediately with a sweep to topple them. Make a melee Strike. If it hits and deals damage, you can attempt an Athletics check to Trip the creature you hit. If you’re wielding a two-handed melee weapon, you can ignore Trip’s requirement that you have a hand free. Both attacks count toward your multiple attack penalty, but the penalty doesn’t increase until after you’ve made both of them.', DEFAULT, JSON_ARRAY('Fighter', 'Flourish'), JSON_OBJECT('Skill','Athletics','Proficiency','trained'))" +
                    ";";


    //static String uniqueSchema; //class-based tables for unique abilities //Rage is an action, Rage is from Barbarian at level 1
    //static String miscSchema; //choice-based tables for miscellaneous-grouped abilities /

    public static void main(String[] args) {
        String completeClassFeatsQuery = //our ideal class feat view
                "SELECT " +
                        "ClassFeat.feat_name, " +
                        "ClassFeat.feat_level, " +
                        "ClassFeat.class_name, " +
                        "Actions.frequency, " +
                        "Actions.action, " +
                        "Actions.`trigger`, " +
                        "Actions.requirement," +
                        "Source.rarity," +
                        "Source.traits," +
                        "Source.description \n" +
                        "FROM ClassFeat \n" +
                        "LEFT JOIN Source " +
                        "ON ClassFeat.feat_name = Source.name AND ClassFeat.feat_level = Source.level \n" +
                        "LEFT JOIN Action AS Actions " +
                        "ON ClassFeat.feat_name = Actions.action_name AND ClassFeat.feat_level = Actions.action_level \n" +
                        "ORDER BY ClassFeat.feat_level, ClassFeat.feat_name;";

        try {
            for (Field field : Main.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)) {
                    String value = (String) field.get(null);
                    //System.out.println("+++ " + field.getName() + " +++");
                    System.out.println(value);
                    //System.out.println();
                }
            }
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
}
