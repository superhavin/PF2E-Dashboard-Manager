import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class schemaBuilder {

    /**
     * Takes a sql script file, and runs it
     * @param connection
     * @param theFile
     * @throws SQLException
     * @throws IOException
     */
    public static void schemaFileToSql(final Connection connection, final String theFile) throws SQLException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(theFile));
             Statement statement = connection.createStatement();) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while((line = bufferedReader.readLine()) != null){
                line = line.trim();

                if(line.isEmpty() ||  line.startsWith("--") || line.startsWith("//") || line.startsWith("#")){continue;}

                sqlBuilder.append(line).append(" ");

                if(line.endsWith(";")){
                    String sql = sqlBuilder.toString().trim();
                    sql = sql.substring(0, sql.length() - 1); // to remove trailing ;

                    System.out.println("Executing: " + sql);
                    statement.execute(sql);

                    sqlBuilder.setLength(0); //reset builder
                }
            }
        }
    }


}
