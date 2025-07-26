import java.sql.*;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/kamaukevin";
    private static final String user = "kevin";
    private static final String password = "null";

    public static void main(String[] args) {

        String ancestry = "CREATE TABLE IF NOT EXIST Ancestry(ancestry_name TEXT UNIQUE NOT NULL);" +
                "INSERT INTO Ancestry(ancestry_name) VALUES" +
                "('Dwarf'), ('Elf'), ('Gnome'), ('Goblin'), " +
                "('Halfling'), ('Human'), ('Leshy'), ('Orc');";

        String skills = "CREATE TABLE IF NOT EXIST Skills(" +
                "skill_name TEXT NOT NULL," +
                "proficiency_rank ENUM('untrained', 'trained', 'expert', 'master', 'legendary') NOT NULL" +
                ");";

        String stats = "CREATE TABLE IF NOT EXIST Stats(" +
                "stat_name TEXT NOT NULL," +
                "ability_boost DECIMAL(2,1) NOT NULL" +
                ");";

        String classes = "CREATE TABLE IF NOT EXIST Class(class_name TEXT UNIQUE NOT NULL);" +
                "INSERT INTO Class(class_name) VALUES" +
                "('Bard'), ('Cleric'), ('Druid'), ('Fighter'), " +
                "('Ranger'), ('Rogue'), ('Witch'), ('Wizard');";

        String classFeats = "CREATE TABLE IF NOT EXISTS ClassFeats(" +
                "name TEXT NOT NULL," +
                "level INT NOT NULL," +
                "ancestry ancestry_name TEXT," +
                "FOREIGN KEY (ancestry_name) REFERENCES Ancestry(ancestry_name)," +

                "stats stat_name TEXT," +
                "FOREIGN KEY (stat_name) REFERENCES Stats(stat_name)." +

                "skills skill_name TEXT," +
                "FOREIGN KEY (skill_name) REFERENCES Skills(skill_name)," +

                "class class_name TEXT," +
                "FOREIGN KEY (class_name) REFERENCES Class(class_name)," +

                "requirement TEXT," +
                "rarity ENUM('Common', 'Uncommon', 'Rare', 'Unique')," +
                "traits TEXT," +
                "description LONGTEXT" +
                ");";

        String query = "SELECT * FROM ClassFeats";

        System.out.println(skills);
        System.out.println(stats);
        System.out.println(classFeats);
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