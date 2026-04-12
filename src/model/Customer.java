package model;

public class Customer extends User {
    
    public Customer(String username, String password) {
        super(username, password, "customer");
    }

    public Customer(int id, String username, String password) {
        super(id, username, password, "customer");
    }

    // Methods from UML diagram
    public void searchFlight() {
        System.out.println("Customer searching flights...");
    }

    public void bookFlight() {
        System.out.println("Customer booking a flight...");
    }

    public void manageBookings() {
        System.out.println("Customer managing own bookings...");
    }

    public void viewTickets() {
        System.out.println("Customer viewing tickets...");
    }

    @Override
    public void viewDashboard() {
        System.out.println("Routing to Customer Dashboard View...");
    }
}
