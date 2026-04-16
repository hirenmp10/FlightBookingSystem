package db;

import java.sql.*;
import db.DBConnection;

/**
 * Utility to synchronize flight available_seats with actual bookings.
 */
public class SyncSeats {
    public static void main(String[] args) {
        System.out.println("Starting Seat Synchronization...");
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return;
            }

            // 1. Get all flights
            String flightSql = "SELECT flightNumber, total_seats, available_seats FROM flights";
            try (Statement flightStmt = conn.createStatement();
                 ResultSet flightRs = flightStmt.executeQuery(flightSql)) {
                
                while (flightRs.next()) {
                    String flightNo = flightRs.getString("flightNumber");
                    int totalSeats = flightRs.getInt("total_seats");
                    int currentAvailable = flightRs.getInt("available_seats");

                    // 2. Count booked seats for this flight
                    int bookedCount = 0;
                    String bookingSql = "SELECT seat_number FROM bookings WHERE flight_number = ?";
                    try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSql)) {
                        bookingStmt.setString(1, flightNo);
                        try (ResultSet bookingRs = bookingStmt.executeQuery()) {
                            while (bookingRs.next()) {
                                String seats = bookingRs.getString("seat_number");
                                if (seats != null && !seats.trim().isEmpty()) {
                                    bookedCount += seats.split(",").length;
                                }
                            }
                        }
                    }

                    int correctAvailable = totalSeats - bookedCount;

                    if (currentAvailable != correctAvailable) {
                        System.out.println("Fixing Flight " + flightNo + ": Found " + bookedCount + " bookings. Correcting available_seats from " + currentAvailable + " to " + correctAvailable);
                        
                        String updateSql = "UPDATE flights SET available_seats = ? WHERE flightNumber = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, correctAvailable);
                            updateStmt.setString(2, flightNo);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        System.out.println("Flight " + flightNo + ": OK (Available: " + correctAvailable + ")");
                    }
                }
            }
            
            System.out.println("Seat Synchronization Complete.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
