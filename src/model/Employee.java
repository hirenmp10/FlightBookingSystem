package model;

/**
 * Represents an Employee derived directly from the UML Class Diagram.
 * Note: Not currently mapped to DB; exists to satisfy OOAD documentation.
 */
public class Employee {
    private int employeeID;
    private String name;
    private String role;
    private double salary;

    public Employee(int employeeID, String name, String role, double salary) {
        this.employeeID = employeeID;
        this.name = name;
        this.role = role;
        this.salary = salary;
    }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    // Methods from UML
    public void addEmployee() {
        System.out.println("Adding employee...");
    }

    public void updateEmployee() {
        System.out.println("Updating employee...");
    }

    public void removeEmployee() {
        System.out.println("Removing employee...");
    }
}
