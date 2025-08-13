import java.lang.reflect.Parameter;
import java.sql.*;

interface AbstractConnector {

    /**
     * Grabs a table from the purposes of a dropdown menu.
     * @param theTable the name of the Table.
     * @return a collection for dropdown menu.
     */
    String[] grabDropdownMenu(final String theTable);

    /**
     * Takes (by invalidating) away options from a dropdown menu.
     * @param theTable the name of the Table.
     * @param theOption the name of the Option.
     * @return status of limitation.
     * true if able to invalidate.
     * false if unable to invalidate.
     */
    boolean limitMenu(final String theTable, final String theOption);

    /**
     * Runs a schema into the database.
     * @param theQuery string of the query.
     */
    void runSchema(final String theQuery);


    /**
     * For running queries which return tuples (into System)
     * @param theQuery string of the query.
     * @param theDatabase connection to the database.
     */
    private static String[] runQuery(final String theQuery, Connection theDatabase){
        String[] queryData;
        try(Connection connection = theDatabase;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(theQuery)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            queryData = new String[columnCount];

            while(resultSet.next()){
                for(int col = 1; col <= queryData.length; col++) {
                    int i = col - 1;
                    //System.out.print(metaData.getColumnName(col) + ": " + resultSet.getString(col));
                    queryData[i] = resultSet.getString(col);
                }
            }

            //return queryData;
        } catch (SQLException e) {
            queryData = null;
            e.printStackTrace();
            System.exit(0);
        }
        return queryData;
    }

    /**
     * Grabs information from certain tables in certain columns
     * @param theTable th
     * @param theColumn
     */
    private static void grabTuples(final String theTable, final Connection theDatabase, final String... theColumn) throws SQLException {

        String query = "SELECT ";

        for(int i = 0; i < theColumn.length; i++){
            if((i+1) < theColumn.length){
                query += "?,";
            }else{
                query += "?";
            }
        }

        query += " FROM " + theTable + ";";

        PreparedStatement param = theDatabase.prepareStatement(query);

        for(int col = 1; col <= theColumn.length; col++){
            int i = col - 1;
            param.setString(col, theColumn[i]);
        }

        runQuery(query, theDatabase);
    }
}
