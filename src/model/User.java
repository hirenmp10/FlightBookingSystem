package model;

public abstract class User {
    protected int id;
    protected String username;
    protected String password;
    protected String role;
    
    // Attributes from UML Diagram
    protected String name;
    protected String email;

    // Constructor for registration
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = "";
        this.email = "";
    }

    // Constructor for user fetched from DB
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = "";
        this.email = "";
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // Setters
    public void setRole(String role) { this.role = role; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    // Abstract methods to satisfy Domain Driven Design / Polymorphism from UML
    public abstract void viewDashboard();
}
