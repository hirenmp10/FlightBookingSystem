# ✈️ Aryavarta Airlines — Flight Booking System

> A full-featured, multi-role Flight Booking System built with **Java**, **JavaFX**, and **MySQL** — developed as a Mini Project for UE23CS352B (Object Oriented Analysis & Design) at PES University.

---

## 🚀 Features

### 👤 Admin
- Add new flights (flight number, route, date/time, seats, cost)
- View all system bookings

### 🛂 Manager
- Approve or reject pending flights added by Admins
- Only approved flights are visible to customers

### 👥 Customer
- Register and log in securely
- Search flights by origin and destination
- Interactively select seats on a visual seat map
- Fill passenger details for multi-seat bookings
- View a full payment summary before confirming
- View formatted e-tickets for all bookings
- Cancel bookings (seats automatically restored)

---

## 🏗️ Architecture

This project strictly follows the **MVC (Model-View-Controller)** architecture:

```
src/
├── model/          ← Data classes (User, Flight, Booking, Ticket, Payment, Employee)
├── view/           ← JavaFX FXML UI files (in resources/views/)
├── controller/     ← UI logic (AdminController, CustomerController, BookingController...)
├── dao/            ← Database Access Objects (FlightDAO, BookingDAO, UserDAO)
├── observer/       ← Observer Pattern (EmailObserver, NotificationService)
├── service/        ← Adapter Pattern (PaymentProcessor, StripePaymentAdapter)
├── utils/          ← Factory Pattern (UserFactory), helpers (SceneSwitcher, ViewFactory)
└── db/             ← Singleton DB Connection (DBConnection)
```

---

## 🎨 Design Patterns Used

| Pattern | Type | Class |
|---|---|---|
| **Factory Method** | Creational | `utils/UserFactory.java` |
| **Adapter Pattern** | Structural | `service/StripePaymentAdapter.java` |
| **Observer Pattern** | Behavioral | `observer/EmailObserver.java` |
| **Singleton Pattern** | Extra | `db/DBConnection.java` |

---

## 📐 SOLID Design Principles

| Principle | Where Applied |
|---|---|
| **SRP** — Single Responsibility | `FlightDAO` (DB only) vs `AdminController` (UI only) |
| **OCP** — Open/Closed | Abstract `User` extended by `Admin`, `Customer`, `Manager` |
| **LSP** — Liskov Substitution | `UserFactory` returns subclasses typed as `User` |
| **DIP** — Dependency Inversion | `PaymentProcessor` interface decouples from Stripe |

---

## 🗃️ Database Setup

1. Install **MySQL** and create a database:
```sql
CREATE DATABASE flight_booking_system;
```

2. Run the schema file:
```bash
mysql -u root -p flight_booking_system < flight_booking_schema.sql
```

3. Update credentials in `src/db/DBConnection.java`:
```java
private static final String URL  = "jdbc:mysql://localhost:3306/flight_booking_system";
private static final String USER = "root";
private static final String PASS = "your_password";
```

4. Seed default admin and manager accounts:
```sql
INSERT INTO users (username, password, role) VALUES ('admin', 'admin', 'admin');
INSERT INTO users (username, password, role) VALUES ('manager', 'manager', 'manager');
```

---

## ▶️ How to Run

### Prerequisites
- Java JDK 17+
- JavaFX SDK
- MySQL 8.0+

### Steps
```bash
# Clone the repo
git clone https://github.com/<your-username>/FlightBookingSystem.git
cd FlightBookingSystem

# Run the build script (compiles and launches the app)
build.bat
```

---

## 🔑 Default Login Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Manager | `manager` | `manager123` |
| Customer | Register via the app | — |

---

## 📁 Project Structure

```
FlightBookingSystem/
├── src/                    ← Java source code
├── resources/
│   └── views/              ← JavaFX FXML UI files
├── lib/                    ← External JARs (JavaFX, MySQL connector, JavaMail)
├── flight_booking_schema.sql ← Database schema
├── build.bat               ← Build & run script
└── README.md
```

---

## 👨‍💻 Team Members

| Name | SRN | Module |
|---|---|---|
| Hiren M P | PES2UG23CS224 | Admin Dashboard, Add Flight, MVC |
| Gowtham B | PES2UG23CS205 | Customer Dashboard, Book Flight, Seat Selection |
| Jayarathna R | PES2UG23CS239 | Manager Dashboard, Approve Flights |
| Aisiri H | PES2UG24CS804 | View Tickets, Cancel Booking, Observer Pattern |

---

## 🎓 Academic Details

- **Course:** UE23CS352B — Object Oriented Analysis & Design
- **University:** PES University, Bengaluru
- **Semester:** VI — January to May 2026
