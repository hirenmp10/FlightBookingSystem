package service;

/**
 * Adapter Pattern Implementation
 * This class adapts a hypothetical third-party Stripe API into our generic PaymentProcessor interface.
 * Structural Design Pattern representation for the OOAD checklist.
 */
public class StripePaymentAdapter implements PaymentProcessor {
    
    // Imagine this is a complex 3rd party class: private StripeAPI stripeApi = new StripeAPI();

    @Override
    public boolean processPayment(double amount, String paymentMethodDetails) {
        System.out.println("Adapter engaging...");
        System.out.println("Converting local payment request to Stripe API format.");
        System.out.println("Processing payment of $" + amount + " via Stripe for details: " + paymentMethodDetails);
        
        // Simulate successful charge
        return true; 
    }
}
