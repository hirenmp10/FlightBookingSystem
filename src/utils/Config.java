package utils;

/**
 * Central configuration for the Flight Booking System.
 * Stores database credentials and SMTP settings.
 */
public class Config {
    // Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/flight_booking_system";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "hiren100605";

    // SMTP Configuration
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "465";
    public static final String SMTP_USER = "aryavarta.airlines.noreply@gmail.com";
    public static final String SMTP_PASS = "lprj mhub lmnv gvtc";
}
