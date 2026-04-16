import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdatePrices {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE flights SET cost = cost * 10 WHERE cost < 1000";
            PreparedStatement ps = conn.prepareStatement(sql);
            int rowsUpdated = ps.executeUpdate();
            System.out.println("Flights updated: " + rowsUpdated);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
