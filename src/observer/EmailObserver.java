package observer;

import model.Booking;
import model.Flight;
import db.DBConnection;
import dao.FlightDAO;
import dao.BookingDAO;
import utils.Config;

import javax.mail.*;
import javax.mail.internet.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailObserver implements Observer {
    private String recipientEmail;
    private static final ExecutorService executorTask = Executors.newCachedThreadPool();
    
    public EmailObserver(String email) {
        this.recipientEmail = email;
    }
    
    @Override
    public void update(String eventType, Object data) {
        executorTask.submit(() -> {
            if (data instanceof Booking && "BOOKING_CONFIRMED".equals(eventType)) {
                Booking booking = (Booking) data;
                
                // If booking ID is 0, try to retrieve it from database
                if (booking.getBookingId() == 0) {
                    int retrievedId = retrieveBookingId(booking);
                    if (retrievedId > 0) {
                        booking.setBookingId(retrievedId);
                    }
                }
                
                // Get flight details using DAO
                Flight flightDetails = FlightDAO.getFlightByNumber(booking.getFlightNumber());
                
                // Send email with complete information
                sendBookingConfirmation(booking, flightDetails);
            } else if (data instanceof model.User && "USER_REGISTERED".equals(eventType)) {
                model.User user = (model.User) data;
                sendRegistrationEmail(user);
            }
        });
    }
    
    private int retrieveBookingId(Booking booking) {
        String query = "SELECT id FROM bookings WHERE user_id = ? AND flight_number = ? AND user_name = ?";
        if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
            query += " AND seat_number = ?";
        }
        query += " ORDER BY booking_date DESC LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setInt(1, booking.getUserId());
            statement.setString(2, booking.getFlightNumber());
            statement.setString(3, booking.getUserName());
            
            if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
                statement.setString(4, booking.getSeatNumbers());
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in EmailObserver: " + e.getMessage());
        }
        return 0;
    }
    
    private String extractPassengerName(String passengerData) {
        if (passengerData == null || passengerData.isEmpty()) {
            return "";
        }
        int parenthesisIndex = passengerData.indexOf('(');
        return parenthesisIndex > 0 ? passengerData.substring(0, parenthesisIndex).trim() : passengerData.trim();
    }
    
    private String getExtraPassengers(Booking booking) {
        String extraPassengers = booking.getExtraPassengers();
        if (extraPassengers == null || extraPassengers.isEmpty()) {
            Booking dbBooking = BookingDAO.getBookingById(booking.getBookingId());
            if (dbBooking != null) {
                extraPassengers = dbBooking.getExtraPassengers();
                booking.setExtraPassengers(extraPassengers);
            }
        }
        return extraPassengers;
    }
    
    private int getBookedSeatsCount(Booking booking) {
        if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
            return booking.getSeatNumbers().split(",").length;
        }
        return Math.max(1, booking.getNumSeats());
    }
    
    private void sendBookingConfirmation(Booking booking, Flight flight) {
        if (flight == null) {
            sendSimpleBookingConfirmation(booking);
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        String extraPassengers = getExtraPassengers(booking);
        int seatCount = getBookedSeatsCount(booking);
        double totalCost = flight.getCost() * seatCount;
        
        String subject = "Booking Confirmation - Flight " + booking.getFlightNumber();
        
        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(booking.getUserName()).append(",\n\n");
        message.append("Your booking has been confirmed!\n\n");
        message.append("BOOKING DETAILS\n");
        message.append("==============\n");
        message.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        message.append("Flight Number: ").append(flight.getFlightNumber()).append("\n\n");
        
        message.append("FLIGHT INFORMATION\n");
        message.append("=================\n");
        message.append("Route: ").append(flight.getOrigin()).append(" to ").append(flight.getDestination()).append("\n");
        message.append("Departure: ").append(flight.getDepartureTime().format(formatter)).append("\n\n");
        
        message.append("SEAT INFORMATION\n");
        message.append("===============\n");
        message.append("Seat Number(s): ").append(booking.getSeatNumbers()).append("\n\n");
        
        message.append("PASSENGER DETAILS\n");
        message.append("================\n");
        message.append("User ID: ").append(booking.getUserId()).append("\n");
        message.append("Passenger 1: ").append(booking.getUserName()).append("\n");
        
        if (extraPassengers != null && !extraPassengers.isEmpty()) {
            String[] additionalPassengers = extraPassengers.split(",");
            for (int i = 0; i < additionalPassengers.length; i++) {
                message.append("Passenger ").append(i + 2).append(": ").append(extractPassengerName(additionalPassengers[i])).append("\n");
            }
        }
        message.append("\n");
        
        message.append("PRICING DETAILS\n");
        message.append("==============\n");
        message.append("Cost per seat: ₹").append(flight.getCost()).append("\n");
        message.append("Number of seats: ").append(seatCount).append("\n");
        message.append("Total cost: ₹").append(totalCost).append("\n\n");
        
        message.append("Thank you for choosing our service!\n\n");
        message.append("Safe travels,\n");
        message.append("The Flight Booking Team");
        
        sendEmail(subject, message.toString());
    }
    
    private void sendSimpleBookingConfirmation(Booking booking) {
        String subject = "Booking Confirmation - Flight " + booking.getFlightNumber();
        String message = "Dear " + booking.getUserName() + ",\n\n" +
                "Your booking has been confirmed!\n" +
                "Booking ID: " + booking.getBookingId() + "\n" +
                "Flight: " + booking.getFlightNumber() + "\n" +
                "Seat(s): " + booking.getSeatNumbers() + "\n\n" +
                "Thank you for choosing our service.\n\n" +
                "Safe travels!";
        
        sendEmail(subject, message);
    }
    
    private void sendRegistrationEmail(model.User user) {
        String subject = "Welcome to Aryavarta Airlines - Registration Successful";
        String message = "Dear " + user.getUsername() + ",\n\n" +
                "Thank you for registering with Aryavarta Airlines!\n" +
                "Your account has been successfully created with the role: " + user.getRole() + ".\n\n" +
                "You can now log in to your account and start booking flights with us.\n\n" +
                "Safe travels,\n" +
                "The Aryavarta Airlines Team";
        
        sendEmail(subject, message);
    }
    
    private void sendEmail(String subject, String messageBody) {
        Properties props = new Properties();
        props.put("mail.smtp.host", Config.SMTP_HOST);
        props.put("mail.smtp.port", Config.SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", Config.SMTP_PORT);
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.connectiontimeout", "5000");
        
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Config.SMTP_USER, Config.SMTP_PASS);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Config.SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageBody);
            
            Transport.send(message);
            System.out.println("Email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }
}