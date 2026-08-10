# ✈️ Flight Booking API

A RESTful backend API for a flight booking application built with **Spring Boot**, **Spring Data JPA**, **Spring Security**, **JWT authentication**, and **MySQL**.

The API provides functionality for user authentication, flight management, seat availability, flight bookings, passengers, and booking cancellation.

---

## 🛠️ Technologies

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT Authentication
* Hibernate
* MySQL
* MySQL Connector/J
* Jakarta Validation
* Lombok
* Swagger / OpenAPI
* Maven
* Docker

---

## 📁 Project Structure

```text
flight-booking-api/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── se/lexicon/flightbooking_api/
│   │   │       │
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── mapper/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# 🔐 Authentication

The API uses **JWT Bearer Authentication**.

Authentication endpoints:

```text
POST /api/auth/register
POST /api/auth/login
```

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

Example:

```json
{
  "email": "john@test.com",
  "password": "password123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

Example:

```json
{
  "email": "john@test.com",
  "password": "password123"
}
```

Successful login returns a JWT:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john@test.com",
  "role": "USER"
}
```

Authenticated requests must include:

```http
Authorization: Bearer <JWT>
```

---

# ✈️ Flight Endpoints

Base URL:

```text
/api/flights
```

### Get all flights

```http
GET /api/flights
```

### Get available flights

```http
GET /api/flights/available
```

### Get a flight by ID

```http
GET /api/flights/{id}
```

---

# 💺 Seat Endpoints

### Get available seats

```http
GET /api/flights/{flightId}/seats
```

### Get seats by class

```http
GET /api/flights/{flightId}/seats/class?seatClass=BUSINESS
```

Supported seat classes include:

```text
ECONOMY
PREMIUM_ECONOMY
BUSINESS
FIRST_CLASS
```

---

# 🎫 Booking Endpoints

Booking operations require authentication.

### Create booking

```http
POST /api/flights/{flightId}/book
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example:

```json
{
  "outboundFlightId": 1,
  "returnFlightId": null,
  "tripType": "ONE_WAY",
  "seatClass": "ECONOMY",
  "passengers": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "passportNumber": "PASS12345",
      "email": "john@test.com"
    }
  ]
}
```

For a round trip:

```json
{
  "outboundFlightId": 1,
  "returnFlightId": 4,
  "tripType": "ROUND_TRIP",
  "seatClass": "ECONOMY",
  "passengers": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "passportNumber": "PASS12345",
      "email": "john@test.com"
    }
  ]
}
```

### Get bookings

Bookings should be retrieved for the authenticated user rather than allowing the client to choose another user's email.

```http
GET /api/flights/bookings//my
Authorization: Bearer <JWT>
```

### Cancel booking

```http
DELETE /api/flights/{bookingId}/cancel
Authorization: Bearer <JWT>
```

The authenticated user is determined from the JWT.

---

# 🗄️ Database

The application uses **MySQL 8**.

Database:

```text
flight_booking_db
```

Main entities:

```text
User
 └── Booking
       ├── Passenger
       ├── Outbound Flight
       └── Return Flight
Flight
 └── FlightSeat
```

---

# 🧩 Database Normalization

The database structure has been designed to reduce duplication and maintain data integrity.

### Users

User authentication information is stored separately from booking information.

```text
User
----
id
email
password
role
```

The user's email and password are not duplicated in every booking.

### Flights

Flight information is stored once and referenced by bookings.

```text
Flight
------
id
flight_number
airline
origin
destination
departure_time
arrival_time
status
```

### Flight Seats

Seats belong to a specific flight.

```text
FlightSeat
----------
id
seat_number
seat_class
price
flight_id
```

### Bookings

Bookings reference the authenticated user and flights.

```text
Booking
-------
id
booking_reference
booking_date
status
trip_type
total_price
user_id
outbound_flight_id
return_flight_id
```

### Passengers

Passenger information is associated with bookings rather than being duplicated inside the booking table.

```text
Passenger
---------
id
first_name
last_name
passport_number
email
booking_id
```

Unique constraints are used where appropriate, for example:

```text
User.email
Passenger.passport_number
Booking.booking_reference
Flight.flight_number
```

---

# 🔄 API URL Changes

The API uses a common `/api` prefix.

### Authentication

```text
/api/auth/register
/api/auth/login
```

### Flights

```text
/api/flights
/api/flights/available
/api/flights/{id}
```

### Seats

```text
/api/flights/{flightId}/seats
/api/flights/{flightId}/seats/class
```

### Bookings

```text
/api/flights/{flightId}/book
/api/flights/bookings/my
/api/flights/{bookingId}/cancel
```

The frontend uses:

```text
http://localhost:8080/api
```

as its API base URL.

---

# 📖 Swagger / OpenAPI

Swagger UI is available when the application is running.

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI documentation:

```text
http://localhost:8080/v3/api-docs
```

Swagger supports JWT authentication through the **Authorize** button.

Enter:

```text
Bearer <your-jwt-token>
```

---

# 🐳 Running with Docker

Build and start the application:

```bash
docker compose up --build
```

Stop the application:

```bash
docker compose down
```

---

# 💻 Running Locally

Make sure MySQL is running and the database exists.

Then run:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API will start on:

```text
http://localhost:8080
```

---

# 🧪 Test Users

When the sample data runner is enabled, sample users can be created.

```text
john@test.com
password123
```

```text
jane@test.com
password123
```

These users can be used to obtain JWT tokens through:

```text
POST /api/auth/login
```

---

# 🔒 Security Design

The application uses:

* Password hashing with `PasswordEncoder`
* JWT authentication
* Spring Security
* Role-based authorization
* Protected booking operations
* Authenticated user identification through `Authentication`

Users should not provide their email to identify themselves when performing authenticated operations.

Instead, the backend obtains the authenticated email from the JWT:

```java
Authentication authentication;
```

or:

```java
authentication.getName();
```

This prevents users from accessing or modifying another user's bookings by simply changing an email parameter.

---

# 🌐 Frontend

The React frontend is maintained in a separate repository:

```text
flight-booking-ui
```

The frontend communicates with this API through:

```text
http://localhost:8080/api
```

---

# 👤 Author

## ✈️ Flight Booking Application

- **Backend:** [GitHub Repository](https://github.com/jayani-athukorala/flight-booking-api)
- **Frontend:** [GitHub Repository](https://github.com/jayani-athukorala/flight-booking-ui)