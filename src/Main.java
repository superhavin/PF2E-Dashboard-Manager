import java.lang.reflect.*;

public class Main {
    private final String url = "jdbc:mysql://127.0.0.1:3306/PATH";
    private final String user = "shelyn";
    private final String password = "The Gardens of Shelyn";

    // -------------------- BASE RULES SCHEMAS --------------------

    //size
    static String sizeSchema =
            "CREATE TABLE IF NOT EXISTS Size(" +
                    "size_type VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT size_title PRIMARY KEY (size_type) " +
                    "); ";

    //ability boost
    static String abilityBoostSchema =
            "CREATE TABLE IF NOT EXISTS AbilityBoost(" +
                    "ability_boost DECIMAL(2,1) DEFAULT 0, " +
                    "CONSTRAINT ability_boost_title PRIMARY KEY (ability_boost) " +
                    ");";


    //ability list
    static String abilitySchema =
            "CREATE TABLE IF NOT EXISTS Ability(" +
                    "ability_name VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT ability_title PRIMARY KEY (ability_name) " +
                    "); " +
                    //ability score list
                    "CREATE TABLE IF NOT EXISTS AbilityScore(" +
                    "ability_name VARCHAR(32) NOT NULL, " +
                    "ability_boost DECIMAL(2,1) DEFAULT 0, " +
                    "CONSTRAINT ability_score_title PRIMARY KEY (ability_name, ability_boost), " +
                    "FOREIGN KEY (ability_name) REFERENCES Ability(ability_name), " +
                    "FOREIGN KEY (ability_boost) REFERENCES AbilityBoost(ability_boost) " +
                    "); ";

    //proficiency
    static String proficiencySchema =
            "CREATE TABLE IF NOT EXISTS Proficiency(" +
                    "proficiency_rank VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT proficiency_rank_title PRIMARY KEY (proficiency_rank) " +
                    "); ";

    //skills list
    static String skillSchema =
            "CREATE TABLE IF NOT EXISTS Skill(" +
                    "skill_name VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT skill_title PRIMARY KEY (skill_name) " +
                    ");" +

                    "CREATE TABLE IF NOT EXISTS SkillRank(" +
                    "skill_name VARCHAR(32) NOT NULL, " +
                    "proficiency_rank VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT skill_rank_title PRIMARY KEY (skill_name, proficiency_rank), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name), " +
                    "FOREIGN KEY (proficiency_rank) REFERENCES Proficiency(proficiency_rank) " +
                    "); ";

    //features list
    static String featureSchema =
            "CREATE TABLE IF NOT EXISTS Feature(" +
                    "feature_name VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT feature_title PRIMARY KEY (feature_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS FeatureRank(" +
                    "feature_name VARCHAR(32) NOT NULL, " +
                    "proficiency_rank VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT feature_rank_title PRIMARY KEY (feature_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name) REFERENCES Feature(feature_name), " +
                    "FOREIGN KEY (proficiency_rank) REFERENCES Proficiency(proficiency_rank) " +
                    ");";

    // -------------------- ABCs SCHEMA --------------------

    //ancestry of character
    static String ancestrySchema =
            "CREATE TABLE IF NOT EXISTS Ancestry(" +
                    "ancestry_name VARCHAR(32) NOT NULL, " + //Unique Name = No Level
                    "hit_points INT NOT NULL, " +
                    "size VARCHAR(32) NOT NULL, " +
                    "speed INT NOT NULL, " +
                    "languages JSON NOT NULL, " + //JSON_ARRAY{"Common", "Orcish"}
                    "vision ENUM('low-light', 'dark', 'greater dark') DEFAULT NULL, " + //if null, normal vision

                    "CONSTRAINT size_limit CHECK(NOT(size = 'huge' OR size = 'gargantuan')), " + //no PCs can be ('huge'), ('gargantuan')

                    "CONSTRAINT ancestry_title PRIMARY KEY (ancestry_name), " +
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
                    //"reference_title JSON DEFAULT NULL, " + //JSON_OBJECT{"GeneralFeat":"Cat Fall", "SpellList":"Innate Cantrip", "Action":"Call on Ancient Blood"}

                    "CONSTRAINT ancestry_heritage_name PRIMARY KEY (ancestry_name, heritage_name), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name) " +
                    "); ";

    static String backgroundSchema =
            "CREATE TABLE IF NOT EXISTS Background(" +
                    "background_name VARCHAR(32) NOT NULL, " +
                    //"reference_title JSON DEFAULT NULL, " + //JSON_OBJECT{"GeneralFeat":"Forager", "GeneralFeat":"Assurance"}

                    "CONSTRAINT background_title PRIMARY KEY (background_name) " +
                    //"FOREIGN KEY (lore_name) REFERENCES Skill(skill_name) " +
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
                    "class_name VARCHAR(32) NOT NULL, " + //Unique Name = No Level
                    "key_ability VARCHAR(32) NOT NULL, " +
                    "hit_points INT NOT NULL DEFAULT 6, " +
                    "additional_skills INT NOT NULL DEFAULT 2, " +
                    "secondary_ability VARCHAR(32) DEFAULT NULL, " +

                    "CONSTRAINT class_title PRIMARY KEY (class_name), " +
                    "FOREIGN KEY (key_ability) REFERENCES Ability(ability_name), " +
                    "FOREIGN KEY (secondary_ability) REFERENCES Ability(ability_name)" +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS ClassProficiency(" + //all features as Untrained, unless class specifies otherwise
                    "class_name VARCHAR(32) NOT NULL, " +
                    "feature_name VARCHAR(32) NOT NULL, " + //INSERT all features
                    "feature_proficiency VARCHAR(32) DEFAULT 'untrained', " +

                    "UNIQUE KEY (class_name, feature_name), " +
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
                    ");";

    static String archetypeSchema =
            "CREATE TABLE IF NOT EXISTS Archetype(" +
                    "archetype_name VARCHAR(32) NOT NULL, " +
                    "dedication_level INT NOT NULL, " + //level of the dedication of the archetype

                    "UNIQUE KEY (archetype_name), " +
                    "CONSTRAINT archetype_title PRIMARY KEY (archetype_name, dedication_level) " +
                    //"CONSTRAINT archetype_title PRIMARY KEY (archetype_name) " +
                    ");";

    // -------------------- UNDERLINING SCHEMAS --------------------

    //tables of actions
    static String actionSchema =
            "CREATE TABLE IF NOT EXISTS `Action`(" +
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

    //tables of source, all things (with descriptions) have sources
    static String sourceSchema =
            "CREATE TABLE IF NOT EXISTS Source(" +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "name VARCHAR(32) NOT NULL, " +
                    "level INT DEFAULT 0, " + //if null, does not have a level attribute

                    "book VARCHAR(64) NOT NULL, " +
                    "page INT NOT NULL, " +
                    "description TEXT NOT NULL, " + //Descriptions which refer to other feats/spells, format it like this: 'Fireball'

                    "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                    "traits JSON DEFAULT NULL, " + //JSON_ARRAY{"General", "Skill"}

                    "prerequisites TEXT DEFAULT NULL, " + //non-referenceable prerequisites
                    //"prerequisites_reference JSON DEFAULT NULL, " + //JSON_OBJECT{"ClassFeat":"Aggressive Block", "ClassFeat":"Brutish Shove"}

                    "PRIMARY KEY (id), " +
                    "UNIQUE KEY title (id, name, level), " +
                    "UNIQUE KEY citation_detail (name, book, page), " + //book + page = not unique
                    "UNIQUE KEY description_summary (description(256)) " +
                    ");";

    static String traditionSchema =
            "CREATE TABLE IF NOT EXISTS Tradition(" +
                    "tradition_name VARCHAR(32) NOT NULL, " +
                    "CONSTRAINT tradition_title PRIMARY KEY (tradition_name) " +
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
                    "heightened TEXT DEFAULT NULL, " + //JSON_ARRAY{"Heightened (+1) The damage increases by 2d6."} //JSON_ARRAY{"Heightened (4th) You gain a +1 item bonus to saving throws.", "Heightened (6th) The item bonus to AC increases to +2, and you gain a +1 item bonus to saving throws."}

                    "CONSTRAINT spell_title PRIMARY KEY (spell_name, spell_rank, tradition), " +
                    "FOREIGN KEY (tradition) REFERENCES Tradition(tradition_name) " +
                    ");";

    static String spellListSchema = //spell list for classes with spells
            "CREATE TABLE IF NOT EXISTS SpellList(" +
                    "spell_list VARCHAR(32) NOT NULL, " + //name of spellcasting origin (like class_name or archetype_name)
                    "spell_tradition VARCHAR(32) NOT NULL, " +

                    "list_type ENUM('prepared', 'spontaneous', 'focus', 'innate') DEFAULT 'innate', " +
                    "spellcasting_ability VARCHAR(32) DEFAULT 'Charisma', " +

                    "spells_per_level INT DEFAULT 3, " +
                    "cantrips INT DEFAULT 5, " +
                    "granted_spell JSON DEFAULT NULL, " + //JSON_ARRAY{"Fireball"} //might need separate freeSpell Table

                    "UNIQUE KEY SpellList(spell_list), " +
                    "CONSTRAINT spell_list_title PRIMARY KEY (spell_list, spell_tradition), " +
                    "FOREIGN KEY (spell_tradition) REFERENCES Tradition(tradition_name)," +
                    "FOREIGN KEY (spellcasting_ability) REFERENCES Ability(ability_name) " +
                    ");";

    // -------------------- FEATS SCHEMA --------------------

    static String featSchema =
            "CREATE TABLE IF NOT EXISTS Feat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +

                    "archetype_name VARCHAR(32) DEFAULT NULL, " +
                    "ancestry_name VARCHAR(32) DEFAULT NULL, " +
                    "class_name VARCHAR(32) DEFAULT NULL, " +
                    "ability_name VARCHAR(32) DEFAULT NULL, " + //need to allow multiple requirements checks, like +2 STR and +2 CON
                    "ability_boost DECIMAL(2,0) DEFAULT NULL, " +
                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "skill_proficiency VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(skill_proficiency = 'untrained' OR feature_proficiency = 'untrained')), " +

                    "CONSTRAINT feat_title PRIMARY KEY (feat_name, feat_level), " +
                    "FOREIGN KEY (archetype_name) REFERENCES Archetype(archetype_name), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (ability_name, ability_boost) REFERENCES AbilityScore(ability_name, ability_boost), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    //class feats
    static String classFeatSchema =
            "CREATE TABLE IF NOT EXISTS ClassFeat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +
                    "class_name VARCHAR(32) NOT NULL, " +

                    "CONSTRAINT class_feat_title PRIMARY KEY (feat_name, feat_level, class_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name) " +
                    ");";

    //ancestry feats
    static String ancestryFeatSchema =
            "CREATE TABLE IF NOT EXISTS AncestryFeat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +
                    "ancestry_name VARCHAR(32) NOT NULL, " +

                    "CONSTRAINT ancestry_feat_title PRIMARY KEY (feat_name, feat_level, ancestry_name), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name) " +
                    ");";

//    //general feats
//    static String generalFeatSchema =
//            "CREATE TABLE IF NOT EXISTS GeneralFeat(" +
//                    "feat_name VARCHAR(32) NOT NULL, " +
//                    "feat_level INT NOT NULL, " +
//
//                    "CONSTRAINT general_feat_title PRIMARY KEY (feat_name, feat_level) " +
//                    ");";

    //skill feats
    static String skillFeatSchema =
            "CREATE TABLE IF NOT EXISTS SkillFeat(" +
                    "feat_name VARCHAR(32) NOT NULL, " +
                    "feat_level INT NOT NULL, " +
                    "skill_name VARCHAR(32) NOT NULL, " +

                    "skill_proficiency VARCHAR(32) DEFAULT NULL, " + //NULL means the skill choice does not matter or general feat

                    "CONSTRAINT skill_feat_title PRIMARY KEY (feat_name, feat_level, skill_name), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank) " +
                    ");";

    //archetypes feats
    static String archetypeFeatSchema =
            "CREATE TABLE IF NOT EXISTS ArchetypeFeat(" +
                    "feat_name VARCHAR(32) NOT NULL UNIQUE, " +
                    "feat_level INT NOT NULL, " +
                    "archetype_name VARCHAR(32) NOT NULL, " +

                    "CONSTRAINT archetype_feat_title PRIMARY KEY (feat_name, feat_level, archetype_name)," +
                    "FOREIGN KEY (archetype_name) REFERENCES Archetype(archetype_name) " +
                    ");";

    //Options for a class, ancestry, etc. Joins with FreeFeat to provide the feat.
    static String optionSchema =
            "CREATE TABLE IF NOT EXISTS `Option`(" +
                    "option_name VARCHAR(32) NOT NULL, " + //name of option or feat
                    "option_level INT NOT NULL, " +

                    "class_name VARCHAR(32) DEFAULT NULL, " +
                    "background_name VARCHAR(32) DEFAULT NULL, " +
                    "ancestry_name VARCHAR(32) DEFAULT NULL, " +
                    "heritage_name VARCHAR(32) DEFAULT NULL, " +

                    "CONSTRAINT option_title PRIMARY KEY (option_name, option_level), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (background_name) REFERENCES Background(background_name), " +
                    "FOREIGN KEY (ancestry_name, heritage_name) REFERENCES AncestryHeritage(ancestry_name, heritage_name) " +
                    ");";

    //free boost awarded by an option
    static String freeBoostSchema =
            "CREATE TABLE IF NOT EXISTS FreeBoost(" +
                    "option_name VARCHAR(32) NOT NULL, " +
                    "option_level INT NOT NULL, " +

                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "skill_proficiency VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CONSTRAINT free_title PRIMARY KEY (option_name, option_level), " +
                    "FOREIGN KEY (option_name, option_level) REFERENCES `Option`(option_name, option_level), " +
                    "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    ");";

    //free feat awarded by an option
    static String freeFeatSchema =
            "CREATE TABLE IF NOT EXISTS FreeFeat(" +
                    "option_name VARCHAR(32) NOT NULL, " +
                    "option_level INT NOT NULL, " +

                    "feat_name VARCHAR(32) DEFAULT NULL, " + //if feat_name != null -> free feat
                    "feat_level INT DEFAULT NULL, " +

                    "spell_list VARCHAR(32) DEFAULT NULL, " + //a custom (innate) spell list for each free option

                    //"CONSTRAINT free_feat_title PRIMARY KEY (option_name, option_level), " + //no unique, allows multiple choices
                    "FOREIGN KEY (option_name, option_level) REFERENCES `Option`(option_name, option_level), " +
                    "FOREIGN KEY (feat_name, feat_level) REFERENCES Feat(feat_name, feat_level), " +
                    "FOREIGN KEY (spell_list) REFERENCES SpellList(spell_list) " +
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

                "('Dwarfen Lore'), ('Elven Lore'), ('Goblin Lore'), ('Halfling Lore'), ('Leshy Lore'), ('Orc Lore'), " + //ancestries
                "('Abadar Lore'), ('Iomedae Lore'), " + //deities
                "('Demon Lore'), ('Giant Lore'), ('Vampire Lore'), " + //creatures
                "('Astral Plane Lore'), ('Heaven Lore'), ('Outer Rifts Lore'), " + //planes
                "('Hellknights Lore'), ('Pathfinder Society Lore'), " + //organizations
                "('Absalom Lore'), ('Magnimar Lore'), " + //settlements
                "('Mountain Lore'), ('River Lore'), " + //terrains
                "('Alcohol Lore'), ('Baking Lore'), ('Butchering Lore'), ('Cooking Lore'), ('Tea Lore'), " + //items

                "('Academia Lore'), ('Accounting Lore'), ('Architecture Lore'), ('Art Lore'), ('Astronomy Lore'), " +
                "('Carpentry Lore'), ('Circus Lore'), ('Driving Lore'), ('Engineering Lore'), ('Farming Lore'), " +
                "('Fishing Lore'), ('Fortune-Telling Lore'), ('Games Lore'), ('Genealogy Lore'), ('Gladiatorial Lore'), " +
                "('Guild Lore'), ('Heraldry Lore'), ('Herbalism Lore'), ('Hunting Lore'), ('Labor Lore'), " +
                "('Legal Lore'), ('Library Lore'), " +
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
            "INSERT INTO AncestryHeritage(ancestry_name, heritage_name) VALUES" +
                    "('Orc','Badlands Orc'), " +
                    "('Orc','Battle-Ready Orc'), " +
                    "('Orc','Deep Orc'), " +
                    "('Orc','Grave Orc'), " +
                    "('Orc','Hold-Scarred Orc'), " +
                    "('Orc','Rainfall Orc'), " +
                    "('Orc','Winter Orc'), " +

                    "('Leshy','Cactus Leshy'), " +
                    "('Leshy','Fruit Leshy'), " +
                    "('Leshy','Fungus Leshy'), " +
                    "('Leshy','Gourd Leshy'), " +
                    "('Leshy','Leaf Leshy'), " +
                    "('Leshy','Lotus Leshy'), " +
                    "('Leshy','Root Leshy'), " +
                    "('Leshy','Seaweed Leshy'), " +

                    "('Human','Skilled Human'), " +
                    "('Human','Versatile Human'), " +

                    "('Halfling','Gutsy Halfling'), " +
                    "('Halfling','Hillock Halfling'), " +
                    "('Halfling','Jinxed Halfling'), " +
                    "('Halfling','Nomadic Halfling'), " +
                    "('Halfling','Twilight Halfling'), " +
                    "('Halfling','Wildwood Halfling'), " +

                    "('Goblin','Charhide Goblin'), " +
                    "('Goblin','Irongut Goblin'), " +
                    "('Goblin','Razortooth Goblin'), " +
                    "('Goblin','Snow Goblin'), " +
                    "('Goblin','Unbreakable Goblin'), " +

                    "('Gnome','Chameleon Gnome'), " +
                    "('Gnome','Fey-touched Gnome'), " +
                    "('Gnome','Sensate Gnome'), " +
                    "('Gnome','Umbral Gnome'), " +
                    "('Gnome','Wellspring Gnome'), " +

                    "('Elf','Ancient Elf'), " +
                    "('Elf','Arctic Elf'), " +
                    "('Elf','Cavern Elf'), " +
                    "('Elf','Seer Elf'), " +
                    "('Elf','Whisper Elf'), " +
                    "('Elf','Woodland Elf'), " +

                    "('Dwarf','Ancient-Blooded Dwarf'), " +
                    "('Dwarf','Death Warden Dwarf'), " +
                    "('Dwarf','Forge Dwarf'), " +
                    "('Dwarf','Rock Dwarf'), " +
                    "('Dwarf','Strong-Blooded Dwarf')" +
                    ";";

    static String insertBackgrounds =
            "INSERT INTO Background(background_name) VALUES" +
                    "('Acolyte'), ('Acrobat'), ('Animal Whisperer'), ('Artisan'), ('Artist'), ('Bandit'), ('Barkeep'), ('Barrister'), " +
                    "('Bounty Hunter'), ('Charlatan'), ('Cook'), ('Criminal'), ('Cultist'), ('Detective'), ('Emissary'), ('Entertainer'), " +
                    "('Farmhand'), ('Field Medic'), ('Fortune Teller'), ('Gambler'), ('Gladiator'), ('Guard'), ('Herbalist'), ('Hermit'), " +
                    "('Hunter'), ('Laborer'), ('Martial Disciple'), ('Merchant'), ('Miner'), ('Noble'), ('Nomad'), ('Prisoner'), ('Raised by Belief'), " +
                    "('Sailor'), ('Scholar'), ('Scout'), ('Street Urchin'), ('Teacher'), ('Tinker'), ('Warrior')" +
                    ";" +
            /*
             * BackgroundSkill, choices for the background skill
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
             * BackgroundBoost, every background gets an additional free boost
             */
            "INSERT INTO BackgroundBoost(background_name, ability_boost) VALUES" + //BackgroundBoost.ability_boost(NULL) = Free_Boost
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
             * ClassSkill, choices for class skills
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
                    ";";

    // -------------------- BASE DATA INSERTS (Feats) --------------------

    static String insertFeats =
            "INSERT INTO Feat(feat_name, feat_level, archetype_name, ancestry_name, class_name, ability_name, ability_boost, skill_name, skill_proficiency, feature_name, feature_proficiency) VALUES " +
                    "('Adapted Cantrip', 1, NULL, 'Human', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Ancestral Longevity', 1, NULL, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Animal Accomplice', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Animal Elocutionist', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Beast Trainer', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Burn It!', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('City Scavenger', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Cooperative Nature', 1, NULL, 'Human', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Distracting Shadows', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Dwarven Doughtiness', 1, NULL, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Dwarven Lore', 1, NULL, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Dwarven Weapon Familiarity', 1, NULL, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Elven Lore', 1, NULL, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Elven Weapon Familiarity', 1, NULL, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Fey Fellowship', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('First World Magic', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Folksy Patter', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Forlorn', 1, NULL, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('General Training', 1, NULL, 'Human', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Gnome Obsession', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Gnome Weapon Familiarity', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Goblin Lore', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Goblin Scuttle', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Goblin Song', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Goblin Weapon Familiarity', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Grasping Reach', 1, NULL, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Halfling Lore', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Halfling Luck', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Halfling Weapon Familiarity', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Harmlessly Cute', 1, NULL, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Haughty Obstinacy', 1, NULL, 'Human', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Hold Mark', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Illusion Sense', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Iron Fists', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Junk Tinker', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Leshy Lore', 1, NULL, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Leshy Superstition', 1, NULL, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Mountain Strategy', 1, NULL, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Natural Ambition', 1, NULL, 'Human', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Natural Skill', 1, NULL, 'Human', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Nimble Elf', 1, NULL, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Orc Ferocity', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Orc Lore', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Orc Superstition', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Orc Weapon Familiarity', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Otherworldly Magic', 1, NULL, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Prairie Rider', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Razzle-Dazzle', 1, NULL, 'Gnome', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Rock Runner', 1, NULL, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Rough Rider', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Seedpod', 1, NULL, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Shadow of the Wilds', 1, NULL, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Stonemasons Eye', 1, NULL, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Sure Feet', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Titan Slinger', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Tusks', 1, NULL, 'Orc', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unburdened Iron', 1, NULL, 'Dwarf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unconventional Weaponry', 1, NULL, 'Human', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Undaunted', 1, NULL, 'Leshy', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unfettered Halfling', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Unwavering Mien', 1, NULL, 'Elf', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Very Sneaky', 1, NULL, 'Goblin', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Watchful Halfling', 1, NULL, 'Halfling', NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    // a representative set of general feats (these are ancestry/general examples)
                    "('Additional Lore', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Adopted Ancestry', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Armor Proficiency', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Assurance', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Breath Control', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Canny Acumen', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Diehard', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Dubious Knowledge', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Incredible Initiative', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Pet', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Recognize Spell', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Ride', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Seasoned', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Shield Block', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Toughness', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Trick Magic Item', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Weapon Proficiency', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)," +
                    // a set of skill feats
                    "('Intimidating Glare', 1, NULL, NULL, NULL, NULL, NULL, 'Intimidation', 'trained', NULL, NULL), " +
                    "('Terrain Expertise', 1, NULL, NULL, NULL, NULL, NULL, 'Survival', 'trained', NULL, NULL), " +
                    "('Combat Climber', 1, NULL, NULL, NULL, NULL, NULL, 'Athletics', 'trained', NULL, NULL), " +
                    // Fighter class feats subset (as examples)
                    "('Aggressive Block', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Brutish Shove', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Combat Assessment', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Double Slice', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Exacting Strike', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Point-Blank Stance', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Reactive Shield', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Snagging Strike', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Sudden Charge', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Twin Parry', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Vicious Swing', 1, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Felling Strike', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Quick Reversal', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Swipe', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Assisting Shot', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Blade Break', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Combat Grab', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Disarming Stance', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Dueling Parry', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Intimidating Strike', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Lightning Swap', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Lunge', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Rebounding Toss', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Sleek Reposition', 2, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Powerful Shove', 4, NULL, NULL, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL), " +
                    "('Slam Down', 4, NULL, NULL, 'Fighter', NULL, NULL, 'Athletics', 'trained', NULL, NULL)" +
                    "; " +
            /*
             * AncestryFeats
             */
            "INSERT INTO AncestryFeat(feat_name, feat_level, ancestry_name) " +
                    "SELECT feat_name, feat_level, ancestry_name " +
                    "FROM Feat " +
                    "WHERE ancestry_name IS NOT NULL " +
                    ";" +
            /*
             * SkillFeats
             */
            "INSERT INTO SkillFeat(feat_name, feat_level, skill_name, skill_proficiency) " +
                    "SELECT feat_name, feat_level, skill_name, skill_proficiency " +
                    "FROM Feat " +
                    "WHERE skill_name IS NOT NULL " +
                    ";" +
            /*
             * GeneralFeats
              */
//            "INSERT INTO GeneralFeat(feat_name, feat_level) " +
//                    "SELECT feat_name, feat_level " +
//                    "FROM Feat " +
//                    "WHERE (ancestry_name IS NULL) AND (skill_name IS NULL) AND (class_name IS NULL) " + //not within classFeats, skillFeats, or ancestryFeats
//                    ";" +
            /*
             * ClassFeats
             */
            "INSERT INTO ClassFeat(feat_name, feat_level, class_name) " +
                    "SELECT feat_name, feat_level, class_name " +
                    "FROM Feat " +
                    "WHERE class_name IS NOT NULL " +
                    ";" +
            /*
             * ArchetypeFeats
              */
            "INSERT INTO ArchetypeFeat(feat_name, feat_level, archetype_name) " +
                    "SELECT feat_name, feat_level, archetype_name " +
                    "FROM Feat " +
                    "WHERE archetype_name IS NOT NULL " +
                    ";";
    /*
     * Options (FreeBoost + FreeFeat)
     * Options like subclasses of classes, or abilities given by ancestry heritages
     * FreeBoost applies boosts (like feature or skill)
     * FreeFeat gives free feats
     */
    static String option =
            "INSERT INTO `Option`(option_name, option_level, class_name, background_name, ancestry_name, heritage_name) VALUES " +
                    "('Battle-Ready Orc', 1, NULL, NULL, 'Orc', 'Battle-Ready Orc'), " +
                    "('Deep Orc', 1, NULL, NULL, 'Orc', 'Deep Orc'), " +
                    "('Hold-Scarred Orc', 1, NULL, NULL, 'Orc', 'Hold-Scarred Orc'), " +
                    "('Skilled Human', 1, NULL, NULL, 'Human', 'Skilled Human'), " +
                    "('Versatile Human', 1, NULL, NULL, 'Human', 'Versatile Human'), " + //example of a choice based option
                    "('Canny Acumen', 1, NULL, NULL, NULL, NULL)" + //general feat
                    ";";

    static String freeBoost =
            "INSERT INTO FreeBoost(option_name, option_level, skill_name, skill_proficiency, feature_name, feature_proficiency) VALUES " +
                    "('Skilled Human', 1, NULL, 'trained', NULL, NULL) " +
                    ";";

    static String freeFeat =
            "INSERT INTO FreeFeat(option_name, option_level, feat_name, feat_level, spell_list) VALUES " +
                    "('Battle-Ready Orc', 1, 'Intimidating Glare', 1, NULL), " +
                    "('Deep Orc', 1, 'Terrain Expertise', 1, NULL), " +
                    "('Deep Orc', 1, 'Combat Climber', 1, NULL), " +
                    "('Hold-Scarred Orc', 1, 'Diehard', 1, NULL) " +
                    //"('Versatile Human', 1, NULL, NULL, NULL) " + Grabs any general feat
                    ";";


    static String insertActions =
            "INSERT INTO `Action`(action_name, action_level, frequency, action, `trigger`, requirement) VALUES " +
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
                    "('Slam Down', 4, NULL, 'two-action', NULL, NULL)" +
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
                    "('Dueling Parry', 2, 'Player Core', 146, 'Parry with a one-handed weapon for AC bonus.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Intimidating Strike', 2, 'Player Core', 147, 'Strike to damage and attempt to Demoralize.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Lightning Swap', 2, 'Player Core', 147, 'Swap weapons and Strike as part of one action.', DEFAULT, JSON_ARRAY('Fighter','Flourish'), NULL), " +
                    "('Lunge', 2, 'Player Core', 147, 'Extend your reach for a single Strike.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Rebounding Toss', 2, 'Player Core', 148, 'Throw a weapon to rebound and strike again.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Sleek Reposition', 2, 'Player Core', 148, 'Move into perfect position after a Strike.', DEFAULT, JSON_ARRAY('Fighter','Press'), NULL), " +
                    "('Powerful Shove', 4, 'Player Core', 143, 'You can push larger foes around with your attack. You can use Aggressive Block or Brutish Shove against a creature up to two sizes larger than you. When a creature you Shove or knock back with a shield, polearm, or club’s critical specialization effect has to stop moving because it would hit an object, it takes damage equal to your Strength modifier (minimum 1).', DEFAULT, JSON_ARRAY('Fighter','Press'), JSON_OBJECT('ClassFeat', JSON_ARRAY('Aggressive Block','Brutish Shove'))), " +
                    "('Slam Down', 4, 'Player Core', 143, 'You make an attack to knock a foe off balance, then follow up immediately with a sweep to topple them. Make a melee Strike. If it hits and deals damage, you can attempt an Athletics check to Trip the creature you hit. If you’re wielding a two-handed melee weapon, you can ignore Trip’s requirement that you have a hand free. Both attacks count toward your multiple attack penalty, but the penalty doesn’t increase until after you’ve made both of them.', DEFAULT, JSON_ARRAY('Fighter','Flourish'), JSON_OBJECT('Skill','Athletics','Proficiency','trained'))" +
                    ";";

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


        System.out.println(stringOfAllClassProperties(Main.class));
    }

    private static String stringOfAllClassProperties(final Class<?> theClass){
        try {
            StringBuilder value = new StringBuilder();

            for (Field field : theClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    String fieldValue = (String) field.get(null);
                    if(fieldValue != null) value.append(fieldValue).append("\n");
                }
            }

            return value.toString();
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static void invokeAllClassMethods(final Object theClass){
        try {
            Method[] methods = theClass.getClass().getDeclaredMethods();
            for(Method method : methods){
                if(method.isSynthetic()) continue;

                method.setAccessible(true);

                if(method.getParameterCount() == 0){
                    System.out.println("Running " + method.getName());
                    method.invoke(theClass);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }
}
