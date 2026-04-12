package utils;

import model.Admin;
import model.Customer;
import model.Manager;
import model.User;

/**
 * Factory pattern implementation to instantiate the correct User subclass (Admin, Manager, Customer)
 * This gives students a "Creational Design Pattern" to show the evaluator.
 */
public class UserFactory {

    // Factory method for existing DB users
    public static User createUser(int id, String username, String password, String role) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        
        return switch (role.toLowerCase()) {
            case "admin" -> new Admin(id, username, password);
            case "manager" -> new Manager(id, username, password);
            case "customer" -> new Customer(id, username, password);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }

    // Factory method for new users
    public static User createUser(String username, String password, String role) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        
        return switch (role.toLowerCase()) {
            case "admin" -> new Admin(username, password);
            case "manager" -> new Manager(username, password);
            case "customer" -> new Customer(username, password);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}
