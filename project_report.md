# UE23CS352B - Object Oriented Analysis & Design
## Mini Project Report

---

# **Aryavarta Airlines — Flight Booking System**

---

**Submitted by:**

| NAME | SRN |
|------|-----|
| Hiren M P | PES2UG23CS224 |
| Gowtham B | PES2UG23CS205 |
| Jayarathna R | PES2UG23CS239 |
| Aisiri H | PES2UG24CS804 |

**Semester** VI — **Section** _(Your Section)_

**Faculty Name:** _(Faculty Name)_

**January – May 2026**

**DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING**
**FACULTY OF ENGINEERING**
**PES UNIVERSITY**
*(Established under Karnataka Act No. 16 of 2013)*
100ft Ring Road, Bengaluru – 560 085, Karnataka, India

---

## I. Problem Statement

Modern air travel demands a seamless, digitally-driven booking experience that efficiently manages multiple roles, dynamic flight schedules, and real-time seat availability. Manual or fragmented booking systems result in overbooking, poor passenger management, and slow administrative turnaround.

This project aims to design and implement a **multi-role Flight Booking System** called **Aryavarta Airlines**, which allows:
- **Administrators** to add and manage flights in the system.
- **Managers** to oversee and approve flight schedules before they are made available to the public.
- **Customers** to search, book, and cancel flights, as well as view their e-tickets.

The system enforces a structured approval workflow, real-time seat tracking, passenger data management, and automated email notification upon booking — addressing the core pain points of airline reservation management.

---

## II. Key Features

### 1. Major Features
- **Add Flight (Admin)** — Admins can add new flight records including flight number, origin, destination, date/time, seat count, and cost.
- **Book Flight (Customer)** — Customers can search available approved flights, select seats interactively on a **Vertical Cabin Seat Map**, fill passenger details, and confirm bookings.
- **Interactive Seat Selection** — A refined 6-column cabin layout (`A B C [Aisle] D E F`) with "Cockpit" and "Rear" orientation indicators for an intuitive booking experience.
- **Split Phone Input** — Enhanced user experience with separate fields for Country Code (e.g., +91) and the local number, ensuring better international data integrity.
- **Approve Flight (Manager)** — Managers review pending flights submitted by Admins and approve or reject them before they become visible to customers.

### 2. Minor Features
- **Search Flight** — Customers can search flights by origin and destination using dropdown filters.
- **View Ticket** — Customers can view formatted e-tickets for all their confirmed bookings including route, seat, passenger, and cost details.
- **Seat Data Synchronization** — A background technical utility that ensures `available_seats` always matches the actual number of bookings in the database.
- **Centralized Alert Management** — Standardized error and success notifications across all dashboards for a professional UI feel.

---

## III. System Architecture (MVC)

The system strictly follows the **Model-View-Controller (MVC)** architectural pattern:

| Layer | Implementation |
|---|---|
| **Model** | `model/` package — `User.java`, `Flight.java`, `Booking.java`, etc. |
| **View** | `resources/views/` — JavaFX FXML files optimized for widescreen viewing. |
| **Controller** | `controller/` package — Heavy logic for UI events and data flow management. |

---

## IV. Design Principles (SOLID)

### 1. Single Responsibility Principle (SRP)
**Where:** `dao.FlightDAO.java` handles database persistence, while `controller.AdminController.java` handles purely UI events.

### 2. Open/Closed Principle (OCP)
**Where:** The `User` abstract class is closed for modification but open for extension via concrete subclasses like `Admin`, `Customer`, and `Manager`.

### 3. Liskov Substitution Principle (LSP)
**Where:** `UserFactory.java` returns concrete subclasses typed as the parent `User`. The application logic operates on the `User` abstraction seamlessly.

### 4. Dependency Inversion Principle (DIP)
**Where:** The system depends on the `PaymentProcessor.java` interface rather than a concrete implementation like Stripe, making the payment module swappable.

---

## V. Design Patterns

### 1. Creational: Factory Method
**Class:** `utils/UserFactory.java`
Dynamically instantiates role-specific user objects (`Admin`, `Customer`, `Manager`) based on database strings during login.

### 2. Structural: Adapter Pattern
**Class:** `service/StripePaymentAdapter.java`
Adapts a third-party payment gateway to the internal `PaymentProcessor` interface, shielding core logic from API changes.

### 3. Behavioral: Observer Pattern
**Class:** `observer/EmailObserver.java`
Automatically triggers booking confirmation emails whenever the `NotificationService` (Subject) notifies its observers of a successful transaction.

### 4. Extra: Singleton Pattern
**Class:** `db/DBConnection.java`
Guarantees a single database connection instance throughout the application lifecycle to save resources and prevent leaks.

---

## VI. Use Case & UML Diagrams

*(Please refer to the attached UML package for the Use Case, Class, State, and Activity diagrams)*

---

## VII. GitHub Repository

> **Public Codebase:** [https://github.com/hirenmp10/FlightBookingSystem](https://github.com/hirenmp10/FlightBookingSystem)

---

## VIII. Screenshots

*(Add screenshots of the Vertical Seat Map, Split Phone Field, and Admin Dashboards here)*

---

## IX. Viva Oral Examination & Technical FAQs

### 🎯 Key Design Pattern Explanations
- **Factory Method**: "We use it to create User objects based on database roles. It decouples the login logic from the specific subclass being instantiated."
- **Observer Pattern**: "We used this for notifications. When a booking succeeds, the Subject notifies the EmailObserver, which sends the confirmation mail without blocking the main thread."
- **Singleton**: "Used in DBConnection to ensure we don't open hundreds of connections to the database, which would crash the system under high load."

### 🏗️ SOLID Principles Recap
- **LSP**: "My `UserFactory` returns a `User`. Even if it's an `Admin` underneath, the rest of the code treats it as a `User`, proving the subclass can replace the parent."
- **OCP**: "If we want to add a 'Pilot' role, we just extend `User`. We don't have to change any existing user code."

---

## X. Individual Contributions

| Name | Role & Module |
|---|---|
| Hiren M P | Admin Dashboard, MVC Architecture, Database Schema |
| Gowtham B | Seat Selection Map, Phone Split UX, Search Logic |
| Jayarathna R | Manager Approval Workflow, Role Validation |
| Aisiri H | Ticket Cancellation, Email Observer System |
