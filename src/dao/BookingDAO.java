package dao;

import model.Booking;
import db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public static boolean addBooking(Booking booking) {
        String insertSql = "INSERT INTO bookings (user_id, flight_number, user_name, dob, phone, email, address, num_seats, seat_number, extra_passengers) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, booking.getUserId());
                stmt.setString(2, booking.getFlightNumber());
                stmt.setString(3, booking.getUserName());
                stmt.setString(4, booking.formatDob(booking.getDob()));
                stmt.setString(5, booking.getPhone());
                stmt.setString(6, booking.getEmail());
                stmt.setString(7, booking.getAddress());
                stmt.setInt(8, booking.getNumSeats());
                stmt.setString(9, booking.getSeatNumbers());

                String extraPassengers = booking.getExtraPassengersDetails();
                stmt.setString(10, (extraPassengers != null && !extraPassengers.isEmpty()) ? extraPassengers : null);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            booking.setBookingId((int) generatedKeys.getLong(1));
                        }
                    }
                    
                    // Update available seats in the same transaction using the SAME connection
                    if (FlightDAO.updateAvailableSeats(conn, booking.getFlightNumber(), -booking.getNumSeats())) {
                        conn.commit();
                        return true;
                    }
                }
                conn.rollback();
            } catch (SQLException e) {
                try {
                    if (!conn.isClosed()) conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
                throw e; // Rethrow to be caught by outer catch
            }
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
        }
        return false;
    }

    public static boolean cancelBooking(int bookingId, String flightNumber, int seatsToReturn) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String deleteSql = "DELETE FROM bookings WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, bookingId);
                int deleted = deleteStmt.executeUpdate();

                if (deleted > 0) {
                    if (FlightDAO.updateAvailableSeats(conn, flightNumber, seatsToReturn)) {
                        conn.commit();
                        return true;
                    }
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
        }
        return false;
    }

    public static int getTotalSeats(String flightNumber) {
        String sql = "SELECT total_seats FROM flights WHERE flightNumber = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flightNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_seats");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total seats: " + e.getMessage());
        }
        return 0;
    }

    public static boolean isSeatBooked(String flightNumber, int seatNum) {
        String sql = "SELECT seat_number FROM bookings WHERE flight_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flightNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String seatNumbers = rs.getString("seat_number");
                    if (seatNumbers != null) {
                        String[] seats = seatNumbers.split(",");
                        for (String seat : seats) {
                            if (seat.trim().equals(String.valueOf(seatNum))) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking seat booking: " + e.getMessage());
        }
        return false;
    }

    public static List<Booking> getBookingsByUserId(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, flight_number, user_id, num_seats, user_name, dob, phone, email, address, seat_number, extra_passengers FROM bookings WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public static Booking getBookingById(int bookingId) {
        String sql = "SELECT id, flight_number, user_id, num_seats, user_name, dob, phone, email, address, seat_number, extra_passengers FROM bookings WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking(
            rs.getString("flight_number"),
            rs.getString("user_name"),
            rs.getString("dob"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            rs.getInt("num_seats"),
            rs.getInt("user_id"),
            rs.getString("seat_number")
        );
        booking.setBookingId(rs.getInt("id"));
        booking.setExtraPassengers(rs.getString("extra_passengers"));
        return booking;
    }
}
