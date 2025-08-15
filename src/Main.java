import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;

public class Main {
    private final String url = "jdbc:mysql://127.0.0.1:3306/PATH";
    private final String user = "shelyn";
    private final String password = "The Gardens of Shelyn";

    static String sizeSchema =
            "CREATE TABLE IF NOT EXISTS Size(" +
                    "size_type VARCHAR(32) UNIQUE NOT NULL, " +
                    "UNIQUE KEY Size(size_type)" +
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
                    "hit_points INT NOT NULL DEFAULT 6, " +
                    "size VARCHAR(32) NOT NULL DEFAULT 'medium', " +
                    "speed INT DEFAULT 25, " +
                    "languages JSON, " + //'{"Common", "Orcish"}'

                    "CONSTRAINT size_limit CHECK(NOT(size = 'huge' OR size = 'gargantuan')), " + //no PCs can be ('huge'), ('gargantuan')

                    "UNIQUE KEY Ancestry(ancestry_name), " +
                    "FOREIGN KEY (size) REFERENCES Size(size_type) " +
                    ");" +

                    "CREATE TABLE IF NOT EXISTS AncestryBoost(" +
                    "ancestry_name VARCHAR(32) NOT NULL, " +
                    "ability_name VARCHAR(32) NOT NULL, " +
                    "optional_ability BOOLEAN NOT NULL, " +
                    "ability_flaw BOOLEAN NOT NULL, " +

                    "CONSTRAINT optional_or_flaw CHECK(NOT(optional_ability & ability_flaw)), " +  //no setting untrained prof.

                    "UNIQUE KEY(ancestry_name, ability_name), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name), " +
                    "FOREIGN KEY (ability_name) REFERENCES Ability(ability_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS AncestryHeritage(" +
                    "ancestry_name VARCHAR(32) NOT NULL, " +
                    "heritage_name varchar(32) NOT NULL, " +
                    "feat_title JSON DEFAULT NULL, " + //{"GeneralFeat":"Cat Fall", "SpellAbility":"Innate Cantrip"}

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
                    "CONSTRAINT background_title PRIMARY KEY (background_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS BackgroundSkill(" +
                    "background_name VARCHAR(32) NOT NULL, " +
                    "skill_name VARCHAR(32) NOT NULL, " +
                    "optional_skill BOOLEAN NOT NULL, " +

                    "UNIQUE KEY (background_name, skill_name), " +
                    "FOREIGN KEY (background_name) REFERENCES Background(background_name), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS BackgroundBoost(" +
                    "background_name VARCHAR(32) NOT NULL, " +
                    "ability_boost VARCHAR(32) NOT NULL, " +
                    "alternative_boost BOOLEAN NOT NULL, " + //if it has custom boost

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
                    "optional_skill BOOLEAN NOT NULL, " +

                    "UNIQUE KEY (class_name, skill_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name) " +
                    "); " +

                    "CREATE TABLE IF NOT EXISTS ClassOption(" + //class options pointing to additional abilities
                    "class_name VARCHAR(32) NOT NULL, " +
                    "option_level INT NOT NULL, " +
                    "class_option VARCHAR(32) NOT NULL, " + //if class option = class name, then it is a default option
                    "feat_title JSON DEFAULT NULL, " + //{"GeneralFeat":"ShieldBlock", "ClassFeat":"Reactive Strike"}

                    "skill_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_name VARCHAR(32) DEFAULT NULL, " +
                    "feature_proficiency VARCHAR(32) DEFAULT NULL, " +

                    "CHECK(NOT(feature_proficiency = 'untrained')), " +  //no setting untrained prof.

                    "UNIQUE KEY ClassOption(class_name, option_level, class_option), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                    "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name), " +
                    "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
                    "); ";

    //-----------↑Rules↑-----------------------------------------------------↓Character↓--------------------------------
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
                    "level INT, " +
                    "book VARCHAR(64) NOT NULL, " +
                    "page INT NOT NULL, " +
                    "description TEXT NOT NULL, " + //Descriptions which refer to other feats/spells, format it like this: 'Fireball'

                    "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                    "traits JSON DEFAULT NULL, " + //'{"General", "Skill"}'
                    "prerequisites JSON DEFAULT NULL, " + //'{"ClassFeat":"Aggressive Block", "ClassFeat":"Brutish Shove"}'

                    "PRIMARY KEY (id, name), " +
                    "UNIQUE KEY description_summary (description(256)) " +
                    ");";

    //table of spells... (JOIN with ActionTable)
    static String spellSchema = //needs spell slots/charges/focus table, spell abilities (for innate spells)
            "CREATE TABLE IF NOT EXISTS Spell(" +
                    "spell_name VARCHAR(32) NOT NULL, " +
                    "spell_rank INT NOT NULL, " +
                    "tradition ENUM('arcane', 'divine', 'primal', 'occult') NOT NULL, " +

                    "`range` INT DEFAULT NULL, " +
                    "target varchar(64) DEFAULT NULL, " + //'creatures', 'allies', etc.
                    "area varchar(64) DEFAULT NULL, " + //20-foot burst
                    "defense varchar(64) DEFAULT NULL, " +
                    "cast varchar(64) DEFAULT NULL, " +
                    "duration INT DEFAULT NULL, " +
                    "heightened JSON DEFAULT NULL, " + //'{"Heightened (+1)": "The damage increases by 2d6"}'

                    "CONSTRAINT spell_title PRIMARY KEY (spell_name, spell_rank, tradition) " +
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
            "CREATE TABLE IF NOT EXISTS GeneralFeats(" +
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

    //INSERT * FROM GeneralFeats
    //WHERE skill_name IS NOT NULL;
    static String skillFeatSchema =
            "CREATE TABLE IF NOT EXISTS SkillFeats(" +
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

    static String insertRuleSchemas =
            "INSERT INTO Size(size_type) VALUES " +
                    "('tiny'), ('small'), ('medium'), ('large'), ('huge'), ('gargantuan');" +

                    "INSERT INTO AbilityBoost(ability_boost) VALUES" +
                    "(-1.0), (0.0), (1.0), (2.0), (3.0), (4.0), (4.5), (5.0), (5.5), (6.0);" +

                    "INSERT INTO Ability(ability_name) VALUES " +
                    "('Strength'), ('Dexterity'), ('Constitution'), " +
                    "('Intelligence'), ('Wisdom'), ('Charisma');" +

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
                    "('untrained'), ('trained'), ('expert'), ('master'), ('legendary');" +

                    "INSERT INTO Skill(skill_name) " +
                    "VALUES('Acrobatics'), ('Arcana'), ('Athletics'), ('Crafting'), ('Deception'), " +
                    "('Diplomacy'), ('Intimidation'), ('Medicine'), ('Nature'), ('Occultism'), " +
                    "('Performance'), ('Religion'), ('Society'), ('Stealth'), ('Survival'), ('Thievery');" +

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
                    "('martial weapons'), ('advanced weapons'), ('unarmed attacks'), ('spellcasting ability'), " +
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
                    ";" +
                    /*skim default values thru insert*/
                    "INSERT INTO Ancestry(ancestry_name) VALUES " +
                    "('Dwarf'), ('Elf'), ('Gnome'), ('Goblin'), " +
                    "('Halfling'), ('Human'), ('Leshy'), ('Orc')" +
                    ";" +
                    /*skim default values thru insert*/
                    "INSERT INTO Class(class_name, key_ability, secondary_ability) VALUES" +
                    "('Bard', 'Charisma', NULL), ('Cleric', 'Wisdom', NULL), ('Druid', 'Wisdom', NULL), ('Fighter', 'Strength', 'Dexterity'), " +
                    "('Ranger', 'Dexterity', 'Strength'), ('Rogue', 'Dexterity', NULL), ('Witch', 'Intelligence', NULL), ('Wizard', 'Intelligence', NULL), " +
                    //Rogue other ability score is special feature (using AlterTable)
                    "('Barbarian', 'Strength', NULL) " + //Barbarian from Player Core 2
                    "; ";

    static String insertCharacterSchemas =
            /* ***
             * ClassFeats
             */
            "INSERT INTO ClassFeat(feat_name, feat_level, class_name, ability_name, ability_boost, skill_name, skill_proficiency, feature_name, feature_proficiency) VALUES" +
                    "('Vicious Swing', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Determination', 14, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Barreling Charge', 4, 'Fighter', NULL, NULL, 'Athletics', 'trained', NULL, NULL)," +
                    "('Barreling Charge', 4, 'Barbarian', NULL, NULL, 'Athletics', 'trained', NULL, NULL)," +
                    "('Double Slice', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Exacting Strike', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Point Blank Stance', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Reactive Shield', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Snagging Strike', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Sudden Charge', 1, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Sudden Charge', 1, 'Barbarian', NULL, NULL, NULL, NULL, NULL, NULL)," +
                    "('Powerful Shove', 4, 'Fighter', NULL, NULL, NULL, NULL, NULL, NULL)" +
                    ";" +
                    /* Action for ClassFeats */
                    "INSERT INTO Action(action_name, action_level, frequency, action, `trigger`, requirement) VALUES" +
                    "('Vicious Swing', 1, NULL, 'two-action', NULL, NULL), " +
                    "('Determination', 14, 'once per day', 'one-action', NULL, NULL), " +
                    "('Barreling Charge', 4, NULL, 'two-action', NULL, NULL), " +
                    "('Double Slice', 1, NULL, 'two-action', NULL, 'You are wielding a ranged weapon'), " +
                    "('Exacting Strike', 1, NULL, 'one-action', NULL, NULL), " +
                    "('Point Blank Stance', 1, NULL, 'one-action', NULL, 'You are wielding a ranged weapon'), " +
                    "('Reactive Shield', 1, NULL, 'reaction', 'An enemy hits you with a melee Strike', 'You are wielding a shield'), " +
                    "('Snagging Strike', 1, NULL, 'one-action', NULL, 'You have one hand free, and your target is within reach of that hand'), " +
                    "('Sudden Charge', 1, NULL, 'two-action', NULL, NULL)" +
                    ";" +
                    /*Source for ClassFeats*/
                    ""
            ;

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

        try{
            for(Field field : Main.class.getDeclaredFields()){
                if(Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)){
                    String value = (String) field.get(null);
                    //System.out.println("+++ " + field.getName() + " +++");
                    System.out.println(value);
                    //System.out.println();
                }
            }
        }catch (IllegalAccessException exception){
            exception.printStackTrace();
        }
    }
}
