package db;

import java.sql.*;
import utils.Config;

public class DBConnection {
    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            synchronized (DBConnection.class) {
                if (!initialized) {
                    try (Connection conn = DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASS)) {
                        initializeDatabase(conn);
                    }
                    initialized = true;
                }
            }
        }
        return DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASS);
    }

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Create Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "username VARCHAR(100) NOT NULL UNIQUE, " +
                         "password VARCHAR(255) NOT NULL, " +
                         "role ENUM('admin', 'customer', 'manager') NOT NULL, " +
                         "email VARCHAR(255))");

            // Create Flights Table
            stmt.execute("CREATE TABLE IF NOT EXISTS flights (" +
                         "flightNumber VARCHAR(6) PRIMARY KEY, " +
                         "origin VARCHAR(100), " +
                         "destination VARCHAR(100), " +
                         "departure_time DATETIME, " +
                         "total_seats INT, " +
                         "available_seats INT, " +
                         "cost DECIMAL(10,2), " +
                         "approved BOOLEAN DEFAULT FALSE)");

            // Create Bookings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "flight_number VARCHAR(6), " +
                         "user_id INT, " +
                         "num_seats INT, " +
                         "booking_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                         "user_name VARCHAR(100), " +
                         "dob DATE, " +
                         "phone VARCHAR(15), " +
                         "email VARCHAR(100), " +
                         "address TEXT, " +
                         "seat_number VARCHAR(255), " +
                         "extra_passengers VARCHAR(255), " +
                         "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                         "FOREIGN KEY (flight_number) REFERENCES flights(flightNumber) ON DELETE CASCADE)");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}
