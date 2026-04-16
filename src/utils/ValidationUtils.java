package utils;

import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public class ValidationUtils {
    
    // Basic email regex pattern
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    // Basic phone regex (Requires +, then 10-15 digits for country code + number)
    private static final String PHONE_REGEX = "^\\+[0-9]{10,15}$";

    /**
     * Validates an email address.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    /**
     * Validates a phone number.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        return Pattern.compile(PHONE_REGEX).matcher(phone).matches();
    }
}
