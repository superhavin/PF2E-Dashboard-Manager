import java.sql.*;

public class Main {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/PF2E";
    private static final String user = "shelyn";
    private static final String password = "The Gardens of Shelyn";

    //proficiency
    static String proficiencySchema =
            "CREATE TABLE IF NOT EXiSTS Proficiency(" +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'untrained', " +
                "UNIQUE KEY proficiency_rank(proficiency_rank) " +
            ");" +
            "INSERT INTO Proficiency(proficiency_rank) VALUES " +
            "('untrained'), ('trained'), ('expert'), ('master'), ('legendary');";

    //ability boost
    static String abilityBoostSchema =
            "CREATE TABLE IF NOT EXISTS AbilityBoost(" +
                    "ability_boost DECIMAL(2,1) DEFAULT 0, " +
                    "UNIQUE KEY ability_boost(ability_boost)" +
            ");" +
            "INSERT INTO AbilityBoost(ability_boost) VALUES" +
            "(-1.0), (0.0), (1.0), (2.0), (3.0), (4.0), (4.5), (5.0), (5.5), (6.0);";

    //ancestry of character
    static String ancestrySchema =
            "CREATE TABLE IF NOT EXISTS Ancestry(" +
                "ancestry_name VARCHAR(32) UNIQUE NOT NULL, " + //Unique Name = No Level
                "hit_points INT NOT NULL DEFAULT 0, " +
                "size ENUM('tiny', 'small', 'medium', 'large', 'huge', 'gargantuan') NOT NULL, " +
                "attribute_boost_1 ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') NOT NULL, " +
                "attribute_boost_2 ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') DEFAULT NULL, " +
                "attribute_flaw ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') DEFAULT NULL, " +
                "languages VARCHAR(128) DEFAULT 'Common', " +
                "ancestry_feature VARCHAR(32) DEFAULT NULL " +
            ");" +
            "INSERT INTO Ancestry(ancestry_name) VALUES " +
            "('Dwarf'), ('Elf'), ('Gnome'), ('Goblin'), " +
            "('Halfling'), ('Human'), ('Leshy'), ('Orc')" +
            ";";
    //skills list
    static String skillSchema =
            "CREATE TABLE IF NOT EXiSTS Skill(" +
                "skill_name VARCHAR(32) NOT NULL, " +
                "UNIQUE KEY skill_name(skill_name) " +
            ");" +
            "INSERT INTO Skill(skill_name) " +
            "VALUES('Acrobatics'), ('Arcana'), ('Athletics'), ('Crafting'), ('Deception'), " +
            "('Diplomacy'), ('Intimidation'), ('Medicine'), ('Nature'), ('Occultism'), " +
            "('Performance'), ('Religion'), ('Society'), ('Stealth'), ('Survival'), ('Thievery');" +
    //skill rank list
            "CREATE TABLE IF NOT EXISTS SkillRank(" +
                "skill_name VARCHAR(32) NOT NULL, " +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'untrained', " +
                "UNIQUE KEY skill_rank(skill_name, proficiency_rank), " +
                "FOREIGN KEY (skill_name) REFERENCES Skill(skill_name), " +
                "FOREIGN KEY (proficiency_rank) REFERENCES Proficiency(proficiency_rank) " +
            "); " +
            "INSERT INTO SkillRank(skill_name, proficiency_rank) " +
            "SELECT skills.skill_name, proficiencies.proficiency_rank " +
            "FROM Skill skills " +
            "CROSS JOIN Proficiency proficiencies " +
            "WHERE NOT EXISTS( " +
                "SELECT * FROM SkillRank skill_ranks " +
                "WHERE skill_ranks.skill_name = skills.skill_name " +
                "AND skill_ranks.proficiency_rank = proficiencies.proficiency_rank " +
            ");";

    //ability list
    static String abilitySchema =
            "CREATE TABLE IF NOT EXISTS Ability(" +
                "ability_name VARCHAR(32) NOT NULL, " +
                "UNIQUE KEY ability_name(ability_name) " +
            "); " +
            "INSERT INTO Ability(ability_name) VALUES " +
            "('Strength'), ('Dexterity'), ('Constitution'), " +
            "('Intelligence'), ('Wisdom'), ('Charisma');" +
    //ability score list
            "CREATE TABLE IF NOT EXISTS AbilityScore(" +
                "ability_name VARCHAR(32) NOT NULL, " +
                "ability_boost DECIMAL(2,1) DEFAULT 0, " +
                "UNIQUE KEY ability_score(ability_name, ability_boost)" +
            "); " +
            "INSERT INTO AbilityScore(ability_name, ability_boost) " +
            "SELECT abilities.ability_name, boosts.ability_boost " +
            "FROM Ability abilities " +
            "CROSS JOIN AbilityBoost boosts " +
            "WHERE NOT EXISTS( " +
                    "SELECT * FROM AbilityScore ability_scores " +
                    "WHERE ability_scores.ability_name = abilities.ability_name " +
                    "AND ability_scores.ability_boost = boosts.ability_boost " +
            ");";

    //class of character
    static String classSchema = "CREATE TABLE IF NOT EXISTS Class(" +
            "class_name VARCHAR(32) UNIQUE NOT NULL, " + //Unique Name = No Level
            "hit_points INT NOT NULL DEFAULT 0, " +
            "key_attribute ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') NOT NULL, " +

            "class_feature VARCHAR(1028) DEFAULT NULL, " + //if class has subclass insert into class_feature

            "perception_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "fortitude_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "reflex_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "will_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "trained_skills VARCHAR(128), " + //'arcana,nature,occultism,religion'
            "unarmed_attacks ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "simple_weapons ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "martial_weapons ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "advanced_weapons ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "unarmored_defense ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "light_armor ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "medium_armor ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "heavy_armor ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained', " +
            "class_dc ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'trained'" +
            "); " +

            "INSERT INTO Class(class_name) VALUES" + //Player Core 1 and 2
            "('Bard'), ('Cleric'), ('Druid'), ('Fighter'), " +
            "('Ranger'), ('Rogue'), ('Witch'), ('Wizard')," +
            "('Alchemist'), ('Barbarian'), ('Champion'), ('Investigator')," +
            "('Monk'), ('Oracle'), ('Sorcerer'), ('Swashbuckler')" +
            ";";

    //features list
    static String featureSchema =
            "CREATE TABLE IF NOT EXISTS Feature(" +
                "feature_name VARCHAR(32) NOT NULL, " +
                "UNIQUE KEY feature_name(feature_name) " +
            "); " +
            "INSERT INTO Feature(feature_name) VALUES" +
            "('martial weapons'), ('advanced weapons'), ('unarmed attacks'), ('spellcasting ability'), " +
            "('unarmored defense'), ('light armor'), ('medium armor'), ('heavy armor'), " +
            "('fortitude saves'), ('reflex saves'), ('will saves'), ('perception'), ('class dc')" +
            ";" +
    //feature rank list
            "CREATE TABLE IF NOT EXISTS FeatureRank(" +
                "feature_name VARCHAR(32) NOT NULL, " +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'untrained', " +
                "UNIQUE KEY feature_rank(feature_name, proficiency_rank) " +
            ");" +
            "INSERT INTO FeatureRank(feature_name, proficiency_rank) " +
            "SELECT features.feature_name, proficiencies.proficiency_rank " +
            "FROM Feature features " +
            "CROSS JOIN Proficiency proficiencies " +
            "WHERE NOT EXISTS(" +
                "SELECT * FROM FeatureRank feature_ranks " +
                "WHERE feature_ranks.feature_name = features.feature_name " +
                "AND feature_ranks.proficiency_rank = proficiencies.proficiency_rank " +
            ");";

    //class feats
    static String classFeatSchema =
            "CREATE TABLE IF NOT EXISTS ClassFeat(" +
            "name VARCHAR(32) NOT NULL, " +
            "level INT NOT NULL, " +
            "class_name VARCHAR(32) NOT NULL, " +

            "ability_name VARCHAR(32) DEFAULT NULL, " +
            "ability_boost DECIMAL (2,0) DEFAULT NULL, " +
            "skill_name VARCHAR(32) DEFAULT NULL, " +
            "skill_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT NULL, " +
            "feature_name VARCHAR(32) DEFAULT NULL, " +
            "feature_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT NULL, " +
            "feat_prerequisites VARCHAR(1028) DEFAULT NULL, " +

            "CONSTRAINT class_feat_title PRIMARY KEY (name, level, class_name), " +
            "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
            "FOREIGN KEY (ability_name, ability_boost) REFERENCES AbilityScore(ability_name, ability_boost), " +
            "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES SkillRank(skill_name, proficiency_rank), " +
            "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES FeatureRank(feature_name, proficiency_rank) " +
            ");" +
    //insert feats into class feat table
            "\nINSERT INTO ClassFeat(name, level, class_name, ability_name, ability_boost,  skill_name, skill_proficiency, feature_name, feature_proficiency) VALUES" +
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
            ";";

    //tables of actions
    static String actionSchema =
            "CREATE TABLE IF NOT EXISTS Action(" +
            "name VARCHAR(32) NOT NULL, " +
            "level INT NOT NULL, " +

            "requirement VARCHAR(1028) DEFAULT NULL, " +
            "frequency VARCHAR(32) DEFAULT NULL," +
            "action ENUM('one-action', 'two-action', 'three-action', " +
            "'reaction', 'free-action', 'first-action to three-action', " +
            "'first-action or two-action', 'two-action or three-action') DEFAULT NULL, " +
            "`trigger` VARCHAR(1028) DEFAULT NULL, " +

            "CONSTRAINT action_title PRIMARY KEY (name, level)" +
            ");" +
    //insert actions into action table
            "\nINSERT INTO Action(name, level, frequency, action, `trigger`, requirement) VALUES" +
            "('Vicious Swing', 1, NULL, 'two-action', NULL, NULL), " +
            "('Determination', 14, 'once per day', 'one-action', NULL, NULL), " +
            "('Barreling Charge', 4, NULL, 'two-action', NULL, NULL), " +
            "('Double Slice', 1, NULL, 'two-action', NULL, 'You are wielding a ranged weapon'), " +
            "('Exacting Strike', 1, NULL, 'one-action', NULL, NULL), " +
            "('Point Blank Stance', 1, NULL, 'one-action', NULL, 'You are wielding a ranged weapon'), " +
            "('Reactive Shield', 1, NULL, 'reaction', 'An enemy hits you with a melee Strike', 'You are wielding a shield'), " +
            "('Snagging Strike', 1, NULL, 'one-action', NULL, 'You have one hand free, and your target is within reach of that hand'), " +
            "('Sudden Charge', 1, NULL, 'two-action', NULL, NULL);";

    //tables of source [WIP]
    static String sourceSchema =
            "CREATE TABLE IF NOT EXISTS Source(" +
                    "name VARCHAR(32) NOT NULL, " +
                    "level INT NOT NULL, " +
                    "book VARCHAR(64) NOT NULL, " +
                    "page INT NOT NULL, " +

                    //duplicate feats from 2 different classes or archetypes
                    "ancestry_name VARCHAR(32) DEFAULT NULL, " +
                    "class_name VARCHAR(32) DEFAULT NULL" +

                    "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                    "traits VARCHAR(128), " + //SELECT SUBSTRING_INDEX('flourish,press,concentrate', ',', 1);
                    "description TEXT, " +

                    "UNIQUE KEY description_summary (description(256)), " +
                    "CONSTRAINT feat_title PRIMARY KEY (name, level), " +
                    "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name), " +
                    "FOREIGN KEY (class_name) REFERENCES Class(class_name) " +
                    ");";

    //ancestry feats [WIP]
    static String ancestryFeatSchema =
            "CREATE TABLE IF NOT EXISTS AncestryFeat(" +
                "name VARCHAR(32) NOT NULL UNIQUE," +
                "level INT NOT NULL," +
                "ancestry_name VARCHAR(32)," +

                "skill_name VARCHAR(32)," +
                "skill_proficiency VARCHAR(16), " +
                "stat_name VARCHAR(32)," +
                "stat_score DECIMAL (2,0), " +
                "feature_name VARCHAR(32)," +
                "feature_proficiency VARCHAR(16)" +
                "requirement VARCHAR(128)," +
                "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common'," +
                "traits VARCHAR(128)," +
                "description TEXT, " +

                "UNIQUE KEY description_summary (description(256))" +
                "CONSTRAINT ancestry_feat_title PRIMARY KEY (name, level, ancestry_name)," +
                "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name)," +
                "FOREIGN KEY (stat_name, stat_score) REFERENCES Stat(stat_name, ability_score)," +
                "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES Skill(skill_name, proficiency_rank)," +
                "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES Feature(feature_name, proficiency_rank)" +
            ");";

    //archetypes feats [WIP]
    static String archetypeSchema =
            "CREATE TABLE IF NOT EXIST Archetype(" +
                "name VARCHAR(32) NOT NULL UNIQUE, " +
                "level INT NOT NULL, " +
                "ancestry_name ENUM('Dwarf', 'Elf', 'Gnome', 'Goblin', 'Halfling', 'Human', 'Leshy', 'Orc') NOT NULL, " +
                "ancestry_features VARCHAR(16), " + //like having permanent wings
                "skill_name VARCHAR(32), " +
                "skill_proficiency VARCHAR(16), " +
                "stat_name VARCHAR(32), " +
                "stat_score DECIMAL (2,0), " +
                "class_name ENUM('Bard', 'Cleric', 'Druid', 'Fighter', 'Ranger', 'Rogue', 'Witch', 'Wizard') DEFAULT NULL" +
                "feature_name VARCHAR(32), " +
                "feature_proficiency VARCHAR(16)" +
                "requirement VARCHAR(128), " +
                "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                "traits VARCHAR(128), " +
                "description TEXT, " +
                "UNIQUE KEY description_summary (description(256))" +
                "CONSTRAINT feat_title PRIMARY KEY (name, level, ancestry_name), " +
                "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name)," +
                "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                "FOREIGN KEY (stat_name, stat_score) REFERENCES Stat(stat_name, ability_score), " +
                "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES Skill(skill_name, proficiency_rank), " +
                "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES Feature(feature_name, proficiency_rank)" +
            ");";

    //queries
    static String testQueries =
            "SELECT * FROM Ancestry;\n" +
            "SELECT * FROM Skill;\n" +
            "SELECT * FROM Stat;\n" +
            "SELECT * FROM Class;\n" +
            "SELECT * FROM Feature;\n" +
            "SELECT * FROM ClassFeat;\n" +
            "SELECT * FROM Action;";

    static String completeClassFeats =
            "SELECT " +
                    "ClassFeat.name, " +
                    "ClassFeat.level, " +
                    "GROUP_CONCAT(DISTINCT ClassFeat.class_name) AS classes, " +
                    "Action.frequency, " +
                    "Action.action, " +
                    "Action.trigger," +
                    "Action.requirement\n" +
            "FROM ClassFeat\n" +
            "JOIN Action ON ClassFeat.name = Action.name AND ClassFeat.level = Action.level\n" +
            "GROUP BY ClassFeat.level, ClassFeat.name;";


    public static void main(String[] args) {
        //testing schemas and queries
        System.out.println(proficiencySchema);
        System.out.println(abilityBoostSchema);

        System.out.println(ancestrySchema);
        System.out.println(skillSchema);
        System.out.println(abilitySchema);
        System.out.println(classSchema);
        System.out.println(featureSchema);

        System.out.println(classFeatSchema);
        System.out.println(actionSchema);
        System.out.println();
        System.out.println(completeClassFeats);
    }

    private static void runQuery(final String theQuery){
        try(Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(theQuery)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while(resultSet.next()){
                for(int col = 1; col <= columnCount; col++) {
                    System.out.print(metaData.getColumnName(col) + ": " + resultSet.getObject(col));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    //user buildQueries
    private static void grabTuples(String theTable, String... theColumn){
        String query = "SELECT ";

        for(int i = 0; i < theColumn.length; i++){
            query += theColumn[i];
            if((i+1) < theColumn.length){
                query += ", ";
            }else{
                query += " ";
            }
        }

        query += "FROM " + theTable + ";";

        runQuery(query);
    }
}