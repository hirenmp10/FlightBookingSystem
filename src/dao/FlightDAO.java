package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Flight;
import db.DBConnection;
import java.time.LocalDateTime;

public class FlightDAO {

    // Add a flight with approved = false (pending approval)
    public static boolean addFlight(Flight flight) {
        String sql = "INSERT INTO flights (flightNumber, origin, destination, departure_time, total_seats, available_seats, cost, approved) VALUES (?, ?, ?, ?, ?, ?, ?, false)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flight.getFlightNumber());
            stmt.setString(2, flight.getOrigin());
            stmt.setString(3, flight.getDestination());
            stmt.setTimestamp(4, Timestamp.valueOf(flight.getDepartureTime()));
            stmt.setInt(5, flight.getTotalSeats());
            stmt.setInt(6, flight.getAvailableSeats());
            stmt.setDouble(7, flight.getCost());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Database error in addFlight: " + e.getMessage());
            return false;
        }
    }

    private static Flight mapResultSetToFlight(ResultSet rs) throws SQLException {
        return new Flight(
            rs.getString("flightNumber"),
            rs.getString("origin"),
            rs.getString("destination"),
            rs.getTimestamp("departure_time").toLocalDateTime(),
            rs.getInt("total_seats"),
            rs.getInt("available_seats"),
            rs.getDouble("cost"),
            rs.getBoolean("approved")
        );
    }

    // Approve a flight (set approved = true)
    public static boolean approveFlight(String flightNumber) {
        String sql = "UPDATE flights SET approved = true WHERE flightNumber = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flightNumber);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Database error in addFlight: " + e.getMessage());
            return false;
        }
    }

    // Delete a flight if not approved by manager
    public static boolean deleteFlight(String flightNumber) {
        String sql = "DELETE FROM flights WHERE flightNumber = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flightNumber);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Database error in addFlight: " + e.getMessage());
            return false;
        }
    }

    // Get all approved flights
    public static List<Flight> getApprovedFlights() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT flightNumber, origin, destination, departure_time, total_seats, available_seats, cost, approved FROM flights WHERE approved = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                flights.add(mapResultSetToFlight(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in getApprovedFlights: " + e.getMessage());
        }

        return flights;
    }

    // Get pending flights (not approved)
    public static List<Flight> getPendingFlights() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE approved = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                flights.add(new Flight(
                    rs.getString("flightNumber"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getTimestamp("departure_time").toLocalDateTime(),
                    rs.getInt("total_seats"),
                    rs.getInt("available_seats"),
                    rs.getDouble("cost"),
                    rs.getBoolean("approved")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database error in getApprovedFlights: " + e.getMessage());
        }

        return flights;
    }

    // Get all flights
    public static List<Flight> getAllFlights() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT flightNumber, origin, destination, departure_time, total_seats, available_seats, cost, approved FROM flights";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                flights.add(mapResultSetToFlight(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in getApprovedFlights: " + e.getMessage());
        }

        return flights;
    }

    // Disapprove a flight
    public static boolean disapproveFlight(String flightNumber) {
        String sql = "DELETE FROM flights WHERE flightNumber = ? AND approved = false"; // only allow disapproving unapproved flights

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flightNumber);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Database error in addFlight: " + e.getMessage());
            return false;
        }
    }

    // Get all unique cities
    public static List<String> getAllCities() {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT origin AS city FROM flights UNION SELECT DISTINCT destination AS city FROM flights";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cities.add(rs.getString("city"));
            }
        } catch (SQLException e) {
            System.err.println("Database error in getApprovedFlights: " + e.getMessage());
        }
        return cities;
    }

    // Search flights based on source, destination, and date (only approved flights)
    public static List<Flight> searchFlights(String source, String destination, LocalDateTime date) {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT flightNumber, origin, destination, departure_time, total_seats, available_seats, cost, approved FROM flights WHERE origin = ? AND destination = ? AND DATE(departure_time) = ? AND approved = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, source);
            stmt.setString(2, destination);
            stmt.setDate(3, Date.valueOf(date.toLocalDate()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    flights.add(mapResultSetToFlight(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in getApprovedFlights: " + e.getMessage());
        }

        return flights;
    }

    public static Flight getFlightByNumber(String flightNumber) {
        String sql = "SELECT flightNumber, origin, destination, departure_time, total_seats, available_seats, cost, approved FROM flights WHERE flightNumber = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, flightNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFlight(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in getApprovedFlights: " + e.getMessage());
        }
        return null;
    }

    public static boolean updateAvailableSeats(String flightNumber, int seatsDelta) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateAvailableSeats(conn, flightNumber, seatsDelta);
        } catch (SQLException e) {
            System.err.println("Database error in updateAvailableSeats: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAvailableSeats(Connection conn, String flightNumber, int seatsDelta) {
        String sql = "UPDATE flights SET available_seats = available_seats + ? WHERE flightNumber = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seatsDelta);
            stmt.setString(2, flightNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error in updateAvailableSeats with connection: " + e.getMessage());
            return false;
        }
    }
}
