import java.sql.*;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/kamaukevin";
    private static final String user = "kevin";
    private static final String password = "null";

    public static void main(String[] args) {


        String classFeatsQuery = "CREATE TABLE IF NOT EXISTS ClassFeats(" +
                "name TEXT NOT NULL," +
                "level INTEGER NOT NULL," +
                "background TEXT" +
                ""; //Working on at the moment

        String query = "SELECT * FROM ClassFeats";

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