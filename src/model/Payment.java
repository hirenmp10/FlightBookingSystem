package model;

/**
 * Represents a Payment derived directly from the UML Class Diagram.
 */
public class Payment {
    private int paymentID;
    private double amount;
    private String paymentMethod;
    private String paymentStatus;

    public Payment(int paymentID, double amount, String paymentMethod, String paymentStatus) {
        this.paymentID = paymentID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    // Getters and setters
    public int getPaymentID() { return paymentID; }
    public void setPaymentID(int paymentID) { this.paymentID = paymentID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    // Domain methods from UML
    public boolean processPayment() {
        this.paymentStatus = "PROCESSED";
        return true;
    }

    public boolean verifyPayment() {
        return this.paymentStatus.equals("PROCESSED");
    }
}
