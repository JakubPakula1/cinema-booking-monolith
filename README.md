# Cinema Booking Monolith

## Overview

A Spring Boot monolithic application for managing cinema bookings, movie listings, and seat reservations. The system provides both customer-facing and admin functionalities with role-based access control.

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 12+
- Git

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone https://github.com/JakubPakula1/cinema-booking-monolith.git
   cd cinema-booking-monolith
   ```

2. **Configure database**
   - Create PostgreSQL database
   - Update `application.properties` or `application.yml` with your database credentials:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/cinema_db
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open browser: `http://localhost:8080`
   - Default admin credentials available in documentation

## Core Features

### Customer Features
- **Movie Repertoire**: Browse available movies with descriptions, ratings, and showtimes
- **Seat Selection**: Interactive seat map for choosing preferred cinema seats
- **Booking Management**: Reserve seats with automatic expiration handling
- **Order Summary**: Review booking details before confirmation
- **Ticket Generation**: Digital tickets with QR codes

### Admin Features
- **Movie Management**: Add, update, and delete movies from catalog
- **Screening Scheduling**: Create and manage movie showtimes
- **Reservation Tracking**: Monitor customer bookings and cancellations

## Key Technical Solutions

### Problem 1: Temporary Reservation Expiration
**Issue**: Reservations needed automatic expiration to prevent seat blocking.

**Solution**: 
- Implemented `TemporaryReservation` entity with expiration timestamps
- `@Transactional` methods automatically refresh expiration time during booking process
- Background cleanup handles expired reservations

### Problem 2: Multipart File Handling in Tests
**Issue**: `NullPointerException` when `MovieFormDTO.getPosterImageFile()` returned null during testing.

**Solution**:
- Added null-safety checks in `AdminMovieViewController`
- Properly configured multipart form data in test requests
- Used `MockMultipartFile` for test file uploads

### Problem 3: Concurrent Seat Reservation Handling
**Issue**: Multiple users could potentially book the same seat simultaneously, causing double-booking conflicts.

**Solution**:
- Implemented `TemporaryReservation` locking mechanism with pessimistic lock
- First user to claim a seat gets immediate reservation while others receive conflict notification
- Database-level constraints prevent duplicate seat reservations
- Automatic reservation expiration frees seats after timeout period (configurable)

## Application Screenshots

### Home Page
![Home Page](screenshots/HomePage.png)

### Movie Repertoire
![Movie Repertoire](screenshots/Repertoire.png)

### Movie Details
![Movie Details](screenshots/MovieDetails.png)

### Movie Management Form
![Movie Form](screenshots/MovieForm.png)

### Seat Selection
![Seat Selection](screenshots/SeatSelection.png)

### Booking Summary
![Summary](screenshots/Summary.png)

### Screening Schedule
![Screening Scheduling](screenshots/ScreeningScheduling.png)

### Orders History
![Orders](screenshots/Orders.png)

### Order Details
![Order](screenshots/Order.png)

### Generated Ticket
![Ticket](screenshots/Ticket.png)

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with role-based access (ADMIN, USER)
- **Database**: PostgreSQL with JPA/Hibernate
- **Testing**: JUnit 5, Mockito, Spring Test
- **Build**: Maven
- **Frontend**: HTML5, CSS3, Bootstrap 5
- **Additional**: Thymeleaf templating

## Project Structure

```
src/
├── main/
│   ├── java/io/github/jakubpakula1/cinema/
│   │   ├── controller/          # MVC controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access layer
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   └── exception/           # Custom exceptions
│   └── resources/
│       ├── templates/           # Thymeleaf templates
│       └── static/              # CSS, JS, images
└── test/
    └── java/...                 # Unit & integration tests
```

## Build & Deploy

```bash
# Build WAR package
mvn clean package

# Run with Maven
mvn spring-boot:run

# Run JAR
java -jar target/cinema-booking-monolith.jar
```

## License

This project is private and for educational purposes.

