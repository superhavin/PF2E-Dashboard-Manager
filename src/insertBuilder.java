import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class insertBuilder {

    /**
     * General-purpose insert method for any table using CSV input.
     * @param connection
     * @param csvFile
     * @param tableName
     * @throws SQLException
     * @throws IOException
     */
    public static void insertAll(final Connection connection, final String csvFile, final String tableName) throws SQLException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile))) {

            String headerLine = bufferedReader.readLine();
            if(headerLine == null){
                throw new IllegalArgumentException("CSV file is empty: " + csvFile);
            }

            String[] columns = headerLine.split(",", -1);

            StringBuilder sql = buildTableHeader(tableName, columns);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
                String line;
                while((line = bufferedReader.readLine()) != null){
                    String[] parts = line.split(",", -1);

                    for(int i = 0; i < columns.length; i++){
                        String value = parts.length > i ? parts[i].trim() : null;

                        if(value == null || value.isEmpty()){ //add a break case to separate inserts
                            preparedStatement.setObject(i + 1, null);
                        } else {
                            if(value.startsWith("[") && value.endsWith("]")
                            || value.startsWith("{") && value.endsWith("}")){ //if JSON
                                preparedStatement.setObject(i + 1, value, Types.OTHER); //stores as JSON
                            } else {
                                preparedStatement.setObject(i + 1, value);
                            }
                        }
                    }

                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    /**
     * Private helper method to build the table header for inserts
     * @param tableName
     * @param columns
     * @return
     */
    private static StringBuilder buildTableHeader(String tableName, String[] columns) {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
        for(int i = 0; i < columns.length; i++){
            sqlBuilder.append(columns[i]);
            if(i < columns.length - 1) sqlBuilder.append(", ");
        }
        sqlBuilder.append(") VALUES (");
        for(int i = 0; i < columns.length; i++){
            sqlBuilder.append("?");
            if(i < columns.length - 1) sqlBuilder.append(", ");
        }
        sqlBuilder.append(")");
        return sqlBuilder;
    }


    private static final String ancestrySql = "INSERT INTO Ancestry (ancestry_name, hit_points, size, speed, languages, vision) VALUES (?, ?, ?, ?, CAST(? AS JSON), ?)";

    /**
     * Specific method which uses Ancestry Table.
     * @param connection
     * @param csvFile
     * @throws SQLException
     * @throws IOException
     */
    public static void insertAncestries(final Connection connection, final String csvFile) throws SQLException, IOException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(ancestrySql);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile))) {

            String line;
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                preparedStatement.setString(1, parts[0]); //ancestry_name
                preparedStatement.setInt(2, Integer.parseInt(parts[1])); //hit_points
                preparedStatement.setString(3, parts[2]); // size
                preparedStatement.setInt(4, Integer.parseInt(parts[3])); //speed

                preparedStatement.setString(5, parts[5].isEmpty() ? "[]" : parts[5]); //languages: treat as JSON as array String
                preparedStatement.setString(6, parts.length > 6 ? parts[6] : null); //vision

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }


}
