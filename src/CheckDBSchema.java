import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class CheckDBSchema {
    public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/flight_booking_system";
        String USER = "root";
        String PASS = "hiren100605";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users LIMIT 1")) {
             
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                System.out.println(rsmd.getColumnName(i) + " - " + rsmd.getColumnTypeName(i));
            }
            
            // Also attempt to execute ALTER TABLE to see if it works, or maybe alter it directly?
            // First let's just see schema.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
