import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AlterDB {
    public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/flight_booking_system";
        String USER = "root";
        String PASS = "hiren100605";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
             
            stmt.executeUpdate("ALTER TABLE users ADD COLUMN email VARCHAR(255)");
            System.out.println("Column 'email' added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
