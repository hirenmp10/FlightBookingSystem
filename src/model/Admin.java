package model;

public class Admin extends User {
    
    public Admin(String username, String password) {
        super(username, password, "admin");
    }

    public Admin(int id, String username, String password) {
        super(id, username, password, "admin");
    }

    // Example of domain logic placed in model per UML diagram
    public void addFlight() {
        System.out.println("Admin adding a flight...");
    }

    public void addAdmin() {
        System.out.println("Admin adding another admin...");
    }

    public void viewBookings() {
        System.out.println("Admin viewing all system bookings...");
    }

    @Override
    public void viewDashboard() {
        System.out.println("Routing to Admin Dashboard View...");
    }
}
