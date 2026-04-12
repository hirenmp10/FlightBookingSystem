package model;

import java.time.LocalDate;

/**
 * Represents a Ticket derived directly from the UML Class Diagram.
 */
public class Ticket {
    private int ticketID;
    private String seatNumber;
    private LocalDate issueDate;

    public Ticket(int ticketID, String seatNumber, LocalDate issueDate) {
        this.ticketID = ticketID;
        this.seatNumber = seatNumber;
        this.issueDate = issueDate;
    }

    // Getters and setters
    public int getTicketID() { return ticketID; }
    public void setTicketID(int ticketID) { this.ticketID = ticketID; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    // Domain methods from UML
    public void generateTicket() {
        System.out.println("Generating physical ticket copy for Seat: " + seatNumber);
    }

    public void viewTicket() {
        System.out.println("Viewing Ticket details ID: " + ticketID);
    }
}
