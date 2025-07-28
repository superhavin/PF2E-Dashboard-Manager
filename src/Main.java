import java.sql.*;

public class Main {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/PF2E";
    private static final String user = "shelyn";
    private static final String password = "The Gardens of Shelyn";

    public static void main(String[] args) {
        //ancestry of character
        String ancestrySchema = "CREATE TABLE IF NOT EXISTS Ancestry(ancestry_name " +
                "ENUM('Dwarf', 'Elf', 'Gnome', 'Goblin', 'Halfling', 'Human', 'Leshy', 'Orc') UNIQUE NOT NULL, " + //Unique Name = No Level

                "hit_points INT NOT NULL DEFAULT 0, " +
                "size ENUM('tiny', 'small', 'medium', 'large', 'huge', 'gargantuan') NOT NULL, " +
                "attribute_boost_1 ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') NOT NULL, " +
                "attribute_boost_2 ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') DEFAULT NULL, " +
                "attribute_flaw ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') DEFAULT NULL, " +
                "languages VARCHAR(128) DEFAULT 'Common', " +
                "ancestry_feature VARCHAR(32) DEFAULT NULL " +
                ");" +

                "INSERT INTO Ancestry(ancestry_name) VALUES" +
                "('Dwarf'), ('Elf'), ('Gnome'), ('Goblin'), " +
                "('Halfling'), ('Human'), ('Leshy'), ('Orc')" +
                ";";
        //skills list
        String skillSchema = "CREATE TABLE IF NOT EXISTS Skill(" +
                "skill_name VARCHAR(32) NOT NULL, " +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'untrained', " +
                "UNIQUE KEY skill_rank(skill_name, proficiency_rank) " +
                "); " +
                "INSERT INTO Skill(skill_name, proficiency_rank) " +
                "SELECT skills.skill_name, proficiencies.proficiency_rank " +
                "FROM(" +
                    "SELECT 'Acrobatics' AS skill_name " +
                    "UNION SELECT 'Arcana' " +
                    "UNION SELECT 'Athletics' " +
                    "UNION SELECT 'Crafting' " +
                    "UNION SELECT 'Deception' " +
                    "UNION SELECT 'Diplomacy' " +
                    "UNION SELECT 'Intimidation' " +
                    "UNION SELECT 'Medicine' " +
                    "UNION SELECT 'Nature' " +
                    "UNION SELECT 'Occultism' " +
                    "UNION SELECT 'Performance' " +
                    "UNION SELECT 'Religion' " +
                    "UNION SELECT 'Society' " +
                    "UNION SELECT 'Stealth' " +
                    "UNION SELECT 'Survival' " +
                    "UNION SELECT 'Thievery' " +
                ")AS skills CROSS JOIN(" +
                    "SELECT 'untrained' AS proficiency_rank " +
                    "UNION SELECT 'trained' " +
                    "UNION SELECT 'expert' " +
                    "UNION SELECT 'master' " +
                    "UNION SELECT 'legendary' " +
                ")AS proficiencies;";
        //ability list
        String statSchema = "CREATE TABLE IF NOT EXISTS Stat(" +
                "stat_name ENUM('Strength', 'Dexterity', 'Constitution', " +
                "'Intelligence', 'Wisdom', 'Charisma') NOT NULL, " +
                "ability_score DECIMAL(2,1) DEFAULT 0, " +
                "UNIQUE KEY ability_boost(stat_name, ability_score)" +
                "); " +
                "INSERT INTO Stat(stat_name, ability_score) " +
                "SELECT stats.stat_name, abilities.ability_score " +
                "FROM(" +
                    "SELECT 'Strength' AS stat_name " +
                    "UNION SELECT 'Dexterity' " +
                    "UNION SELECT 'Constitution' " +
                    "UNION SELECT 'Intelligence' " +
                    "UNION SELECT 'Wisdom' " +
                    "UNION SELECT 'Charisma' " +
                ")AS stats CROSS JOIN(" +
                    "SELECT 0 AS ability_score " +
                    "UNION SELECT 1 " +
                    "UNION SELECT 2 " +
                    "UNION SELECT 3 " +
                    "UNION SELECT 4 " +
                    "UNION SELECT 4.5 " +
                    "UNION SELECT 5 " +
                    "UNION SELECT 5.5 " +
                    "UNION SELECT 6 " +
                ")AS abilities;";
        //class of character
        String classSchema = "CREATE TABLE IF NOT EXISTS Class(class_name ENUM('Bard', 'Cleric'," +
                "'Druid', 'Fighter', 'Ranger', 'Rogue', 'Witch', 'Wizard', 'Alchemist', 'Barbarian', 'Champion'," +
                " 'Investigator', 'Monk', 'Oracle', 'Sorcerer', 'Swashbuckler') UNIQUE NOT NULL, " + //Unique Name = No Level
                "hit_points INT NOT NULL DEFAULT 0, " +
                "key_attribute ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') NOT NULL, " +

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
        String featureSchema = "CREATE TABLE IF NOT EXISTS Feature(feature_name VARCHAR(32) NOT NULL, " +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'untrained', " +
                "UNIQUE KEY feature_rank(feature_name, proficiency_rank) " +
                "); " +
                "INSERT INTO Feature(feature_name, proficiency_rank) " +
                "SELECT features.feature_name, proficiencies.proficiency_rank " +
                "FROM(" +
                    "SELECT 'simple weapons' AS feature_name " +
                    "UNION SELECT 'martial weapons' " +
                    "UNION SELECT 'advanced weapons' " +
                    "UNION SELECT 'unarmed attacks' " +
                    "UNION SELECT 'spellcasting ability' " +
                    "UNION SELECT 'unarmored defense' " +
                    "UNION SELECT 'light armor' " +
                    "UNION SELECT 'medium armor' " +
                    "UNION SELECT 'heavy armor' " +
                    "UNION SELECT 'fortitude saves' " +
                    "UNION SELECT 'reflex saves' " +
                    "UNION SELECT 'will saves' " +
                    "UNION SELECT 'perception' " +
                    "UNION SELECT 'class dc' " +
                ")AS features CROSS JOIN(" +
                    "SELECT 'untrained' AS proficiency_rank " +
                    "UNION SELECT 'trained' " +
                    "UNION SELECT 'expert' " +
                    "UNION SELECT 'master' " +
                    "UNION SELECT 'legendary' " +
                ")AS proficiencies;";
        //class feats
        String classFeatSchema = "CREATE TABLE IF NOT EXISTS ClassFeat(" +
                "name VARCHAR(32) NOT NULL, " +
                "level INT NOT NULL, " +

                "skill_name VARCHAR(32) DEFAULT NULL, " +
                "skill_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT NULL, " +
                "stat_name ENUM('Strength', 'Dexterity', 'Constitution', 'Intelligence', 'Wisdom', 'Charisma') DEFAULT NULL, " +
                "stat_score DECIMAL (2,0) DEFAULT NULL, " +
                "class_name ENUM('Bard', 'Cleric', 'Druid', 'Fighter', 'Ranger', 'Rogue', 'Witch', 'Wizard', " +
                    "'Alchemist', 'Barbarian', 'Champion', 'Investigator', 'Monk', 'Oracle', 'Sorcerer', 'Swashbuckler') NOT NULL, " +
                "feature_name VARCHAR(32) DEFAULT NULL, " +
                "feature_proficiency ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT NULL, " +

                "requirement VARCHAR(128), " +
                "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                "traits VARCHAR(128), " +
                "description TEXT, " +

                "UNIQUE KEY description_summary (description(256)), " +
                "CONSTRAINT feat_title PRIMARY KEY (name, level, class_name), " +
                "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                "FOREIGN KEY (stat_name, stat_score) REFERENCES Stat(stat_name, ability_score), " +
                "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES Skill(skill_name, proficiency_rank), " +
                "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES Feature(feature_name, proficiency_rank)" +
                ");";
        //insert feats into class feat table
        classFeatSchema += "INSERT INTO ClassFeat(name, level, skill_name, skill_proficiency, stat_name, stat_score, class_name, feature_name, feature_proficiency, requirement, rarity, traits, description) VALUES" +
                "('Vicious Swing', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Flourish', NULL)," +
                "('Determination', 14, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Concentrate', NULL)," +
                "('Barreling Charge', 4, 'Athletics', 'trained', NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Flourish', NULL)," +
                "('Barreling Charge', 4, 'Athletics', 'trained', NULL, NULL, 'Barbarian', NULL, NULL, NULL, 'Common', 'Flourish', NULL)," +
                "('Double Slice', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', NULL, NULL)," +
                "('Exacting Strike', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Press', NULL)," +
                "('Point Blank Stance', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'You are wielding a ranged weapon', 'Common', 'Stance', NULL)," +
                "('Reactive Shield', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'You are wielding a shield', 'Common', NULL, NULL)," +
                "('Snagging Strike', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'You have one hand free, and your target is within reach of that hand', 'Common', NULL, NULL)," +
                "('Sudden Charge', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'You charge forward', 'Common', 'Flourish', NULL), " +
                "('Sudden Charge', 1, NULL, NULL, NULL, NULL, 'Barbarian', NULL, NULL, 'You charge forward', 'Common', 'Flourish', NULL)," +
                "('Powerful Shove', 4, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'Aggressive Block, Brutish Shove', 'Common', NULL, 'You can push larger foes around with your attack.')" +
                ";";
        //tables of actions [WIP]
        String actionSchema = "CREATE TABLE IF NOT EXISTS Action(" +
                "name VARCHAR(32) NOT NULL, " +
                "level INT NOT NULL, " +

                "frequency VARCHAR(32)," +
                "action ENUM('one-action', 'two-action', 'three-action', " +
                "'reaction', 'free-action', 'first-action to three-action', " +
                "'first-action or two-action', 'two-action or three-action') NOT NULL, " +
                "`trigger` VARCHAR(1028), " +

                "CONSTRAINT action_title PRIMARY KEY (name,level)" +
                ");";
        //insert actions into action table [WIP]
        actionSchema += "INSERT INTO Action(name, level, frequency, action, `trigger`) VALUES" +
                "('Vicious Swing', 1, NULL, 'two-action', NULL), " +
                "('Determination', 14, 'once per day', 'one-action', NULL), " +
                "('Barreling Charge', 4, NULL, 'two-action', NULL), " +
                "('Double Slice', 1, NULL, 'two-action', NULL), " +
                "('Exacting Strike', 1, NULL, 'one-action', NULL), " +
                "('Point Blank Stance', 1, NULL, 'one-action', NULL), " +
                "('Reactive Shield', 1, NULL, 'reaction', 'An enemy hits you with a melee Strike'), " +
                "('Snagging Strike', 1, NULL, 'one-action', NULL), " +
                "('Sudden Charge', 1, NULL, 'two-action', NULL);";
        //archetypes feats [WIP]
        String archetypeSchema = "CREATE TABLE IF NOT EXIST Archetype(" +
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
        //tables of source [WIP]
        String sourceSchema = "CREATE TABLE IF NOT EXISTS Source(" +
                "name VARCHAR(32) NOT NULL UNIQUE, " +

                "ancestry_name ENUM('Dwarf', 'Elf', 'Gnome', 'Goblin', 'Halfling', 'Human', 'Leshy', 'Orc') DEFAULT NULL, " +
                "class_name ENUM('Bard', 'Cleric', 'Druid', 'Fighter', 'Ranger', 'Rogue', 'Witch', 'Wizard') DEFAULT NULL" + //duplicate feats from 2 different classes or archetypes

                "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                "traits VARCHAR(128), " +
                "description TEXT, " +

                "book VARCHAR(64) NOT NULL, " +
                "page INT NOT NULL, " +
                "CONSTRAINT (name) PRIMARY KEY (name)" +

                "" +
                ");";
        //ancestry feats [WIP]
        String ancestryFeatSchema = "CREATE TABLE IF NOT EXISTS AncestryFeat(" +
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
                "CONSTRAINT feat_title PRIMARY KEY (name,level)," +
                "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name)," +
                "FOREIGN KEY (stat_name, stat_score) REFERENCES Stat(stat_name, ability_score)," +
                "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES Skill(skill_name, proficiency_rank)," +
                "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES Feature(feature_name, proficiency_rank)" +
                ");";

        //queries
        String testQueries =
                "SELECT * FROM Ancestry;\n" +
                "SELECT * FROM Skill;\n" +
                "SELECT * FROM Stat;\n" +
                "SELECT * FROM Class;\n" +
                "SELECT * FROM Feature;\n" +
                "SELECT * FROM ClassFeat;\n" +
                "SELECT * FROM Action;";
        String completeClassFeats =
                "SELECT " +
                        "ClassFeat.name, " +
                        "ClassFeat.level, " +
                        "GROUP_CONCAT(DISTINCT ClassFeat.class_name) AS classes, " +
                        "Action.frequency, " +
                        "Action.action, " +
                        "Action.trigger\n" +
                "FROM ClassFeat\n" +
                "JOIN Action ON ClassFeat.name = Action.name AND ClassFeat.level = Action.level\n" +
                "GROUP BY ClassFeat.level, ClassFeat.name;";

        //testing schemas and queries
        System.out.println(ancestrySchema);
        System.out.println(skillSchema);
        System.out.println(statSchema);
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