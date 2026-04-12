package service;

/**
 * Strategy/Adapter Target Interface
 * Provides a common dependency inversion (DIP) boundary for payment handling.
 */
public interface PaymentProcessor {
    boolean processPayment(double amount, String paymentMethodDetails);
}
