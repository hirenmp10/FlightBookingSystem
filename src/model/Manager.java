package model;

public class Manager extends User {
    
    public Manager(String username, String password) {
        super(username, password, "manager");
    }

    public Manager(int id, String username, String password) {
        super(id, username, password, "manager");
    }

    // Methods from UML diagram
    public void approveFlight() {
        System.out.println("Manager approving flight...");
    }

    public void rejectFlight() {
        System.out.println("Manager rejecting flight...");
    }

    public void manageEmployee() {
        System.out.println("Manager managing employees...");
    }

    @Override
    public void viewDashboard() {
        System.out.println("Routing to Manager Dashboard View...");
    }
}
