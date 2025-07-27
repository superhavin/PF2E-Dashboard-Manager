import java.sql.*;

public class Main {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/PF2E";
    private static final String user = "shelyn";
    private static final String password = "The Gardens of Shelyn";

    public static void main(String[] args) {
        //ancestry of character
        String ancestrySchema = "CREATE TABLE IF NOT EXISTS Ancestry(ancestry_name " +
                "ENUM('Dwarf', 'Elf', 'Gnome', 'Goblin', 'Halfling', 'Human', 'Leshy', 'Orc') DEFAULT 'Human');" +
                "INSERT INTO Ancestry(ancestry_name) VALUES" +
                "('Dwarf'), ('Elf'), ('Gnome'), ('Goblin'), " +
                "('Halfling'), ('Human'), ('Leshy'), ('Orc');";
        //skills of character
        String skillSchema = "CREATE TABLE IF NOT EXISTS Skill(" +
                "skill_name VARCHAR(32) NOT NULL," +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'untrained');" +
                "INSERT INTO Skill(skill_name) VALUES('Acrobatics'), ('Arcana'), ('Athletics'), ('Crafting'), " +
                "('Deception'), ('Diplomacy'), ('Intimidation'), ('Lore'), ('Medicine'), ('Nature'), ('Occultism'), " +
                "('Performance'), ('Religion'), ('Society'), ('Stealth'), ('Survival'), ('Thievery');";
        //ability score of character
        String statSchema = "CREATE TABLE IF NOT EXISTS Stat(" +
                "stat_name ENUM('Strength', 'Dexterity', 'Constitution', " +
                "'Intelligence', 'Wisdom', 'Charisma') UNIQUE NOT NULL," +
                "ability_score DECIMAL(2,1) DEFAULT 0);" +
                "INSERT INTO Stat(stat_name, ability_score) VALUES ('Strength', 0), ('Dexterity', 0), " +
                "('Constitution', 0), ('Intelligence', 0), ('Wisdom', 0), ('Charisma', 0);";
        //class of character
        String classSchema = "CREATE TABLE IF NOT EXISTS Class(class_name ENUM('Bard', 'Cleric'," +
                "'Druid', 'Fighter', 'Ranger', 'Rogue', 'Witch', 'Wizard') UNIQUE NOT NULL);" +
                "INSERT INTO Class(class_name) VALUES" +
                "('Bard'), ('Cleric'), ('Druid'), ('Fighter'), " +
                "('Ranger'), ('Rogue'), ('Witch'), ('Wizard');";
        //class features of character
        String featureSchema = "CREATE TABLE IF NOT EXISTS Feature(feature_name VARCHAR(32) NOT NULL," +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') DEFAULT 'untrained');" +
                "INSERT INTO Feature(feature_name) VALUES('simple weapons'), ('martial weapons'), " +
                "('unarmed attacks'), ('spellcasting'), ('unarmored defense'), ('light armor'), ('medium armor'), " +
                "('heavy armor'), ('fortitude saves'), ('reflex saves'), ('will saves'), ('perception');";
        //class feats
        String classFeatSchema = "CREATE TABLE IF NOT EXISTS ClassFeat(" +
                "name VARCHAR(32) NOT NULL UNIQUE, " +
                "level INT NOT NULL, " +
                "skill_name VARCHAR(32), " +
                "skill_proficiency VARCHAR(16), " +
                "stat_name VARCHAR(32), " +
                "stat_score DECIMAL (2,0), " +
                "class_name VARCHAR(32), " +
                "feature_name VARCHAR(32), " +
                "feature_proficiency VARCHAR(16), " +
                "requirement VARCHAR(128), " +
                "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                "traits VARCHAR(128), " +
                "description TEXT, " +
                "UNIQUE KEY description_summary (description(256)), " +
                "CONSTRAINT feat_title PRIMARY KEY (name, level), " +
                "FOREIGN KEY (class_name) REFERENCES Class(class_name), " + //compatibility error
                "FOREIGN KEY (stat_name, stat_score) REFERENCES Stat(stat_name, ability_score), " +
                "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES Skill(skill_name, proficiency_rank), " +
                "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES Feature(feature_name, proficiency_rank)" +
                ");";
        //insert feats into class feat table
//        classFeatSchema += "INSERT INTO ClassFeat(name, level, skill_name, skill_proficiency, stat_name, stat_score, class_name, feature_name, feature_proficiency, requirement, rarity, traits, description) VALUES" +
//                "('Vicious Swing', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Flourish', NULL)," +
//                "('Determination', 14, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Concentrate', NULL)," +
//                "('Barreling Charge', 4, 'Athletics', 'trained', NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Flourish', 'A forceful rush that knocks enemies aside as you charge.')," +
//                "('Double Slice', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', NULL, NULL)," +
//                "('Exacting Strike', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Press', NULL)," +
//                "('Point Blank Stance', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'You are wielding a ranged weapon', 'Common', 'Stance', NULL)," +
//                "('Reactive Shield', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'You are wielding a shield', 'Common', NULL, NULL)," +
//                "('Snagging Strike', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'You have one hand free, and your target is within reach of that hand', 'Common', NULL, NULL)," +
//                "('Sudden Charge', 1, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, NULL, 'Common', 'Flourish', NULL)," +
//                "('Sudden Charge', 1, NULL, NULL, NULL, NULL, 'Barbarian', NULL, NULL, NULL, 'Common', 'Flourish', NULL)," +
//                "('Powerful Shove', 4, NULL, NULL, NULL, NULL, 'Fighter', NULL, NULL, 'Aggressive Block, Brutish Shove', 'Common', NULL, 'You can push larger foes around with your attack.')" +
//                ";";
        //tables of actions
        String actionSchema = "CREATE TABLE IF NOT EXISTS Action(" +
                "name VARCHAR(32) NOT NULL UNIQUE," +
                "level INT NOT NULL, " +
                "frequency VARCHAR(32)," +
                "action ENUM('one-action', 'two-action', 'three-action', " +
                "'reaction', 'free-action', 'first-action to three-action', " +
                "'first-action or two-action', 'two-action or three-action') NOT NULL," +
                "trigger VARCHAR(1028)," +
                "CONSTRAINT feat_title PRIMARY KEY (name,level)" +
                ");";
        //insert actions into action table
        actionSchema += "INSERT INTO Action(name, level, frequency, action, trigger) VALUES('Vicious Swing', 1, NULL, 'two-action', NULL), " +
                "('Determination', 14, 'once per day', 'first-action', NULL), " +
                "('Barreling Charge', 4, NULL, 'two-action', NULL), " +
                "('Double Slice', 1, NULL, 'two-action', NULL), " +
                "('Exacting Strike', 1, NULL, 'one-action', NULL), " +
                "('Point Blank Stance', 1, NULL, 'one-action', NULL), " +
                "('Reactive Shield', 1, NULL, 'reaction', 'An enemy hits you with a melee Strike'), " +
                "('Snagging Strike', 1, NULL, 'one-action', 'You have one hand free, and your target is within reach of that hand'), " +
                "('Sudden Charge', 1, NULL, 'two-action', NULL);";
        //archetypes feats
        String archetypeSchema = "CREATE TABLE IF NOT EXIST Archetype(" +
                "name VARCHAR(32) NOT NULL UNIQUE, " +
                "level INT NOT NULL, " +
                "ancestry_name VARCHAR(32)," +
                "ancestry_features VARCHAR(16), " + //like having permanent wings
                "skill_name VARCHAR(32), " +
                "skill_proficiency VARCHAR(16), " +
                "stat_name VARCHAR(32), " +
                "stat_score DECIMAL (2,0), " +
                "class_name VARCHAR(32), " +
                "class_features VARCHAR(16), " + //like being of a specific class archetype/subclass
                "feature_name VARCHAR(32), " +
                "feature_proficiency VARCHAR(16)" +
                "requirement VARCHAR(128), " +
                "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique') DEFAULT 'Common', " +
                "traits VARCHAR(128), " +
                "description TEXT, " +
                "UNIQUE KEY description_summary (description(256))" +
                "CONSTRAINT feat_title PRIMARY KEY (name, level), " +
                "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name)," +
                "FOREIGN KEY (class_name) REFERENCES Class(class_name), " +
                "FOREIGN KEY (stat_name, stat_score) REFERENCES Stat(stat_name, ability_score), " +
                "FOREIGN KEY (skill_name, skill_proficiency) REFERENCES Skill(skill_name, proficiency_rank), " +
                "FOREIGN KEY (feature_name, feature_proficiency) REFERENCES Feature(feature_name, proficiency_rank)" +
                ");";
        //tables of source
        String sourceSchema = "CREATE TABLE IF NOT EXISTS Source(" +
                "name VARCHAR(32) NOT NULL UNIQUE, " +
                "book VARCHAR(64) NOT NULL, " +
                "page INT NOT NULL, " +
                "CONSTRAINT (name) PRIMARY KEY (name)" +
                ");";
        //ancestry feats
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

        //String classFeatQuery = "SELECT * FROM ClassFeats";
        String testQuery = "SELECT * FROM Ancestry;\n" +
                "SELECT * FROM Skill;\n" +
                "SELECT * FROM Stat;\n" +
                "SELECT * FROM Class;\n" +
                "SELECT * FROM Feature;\n" +
                "SELECT * FROM ClassFeat";

        System.out.println(ancestrySchema);
        System.out.println(skillSchema);
        System.out.println(statSchema);
        System.out.println(classSchema);
        System.out.println(featureSchema);
        System.out.println(classFeatSchema);
        //System.out.println(action);
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