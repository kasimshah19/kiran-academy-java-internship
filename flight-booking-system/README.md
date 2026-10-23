# Flight Booking System

A production-grade, enterprise-ready Flight Booking and Inventory Management System built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Spring Security (JWT)**, and **Flyway**.

Designed for portfolio review and Java backend technical interviews, this project implements a layered modular monolith with atomic inventory management, strict `BigDecimal` financial calculations, role-based access control, centralized exception handling, OpenAPI/Swagger documentation, Docker containerization, and GitHub Actions CI.

---

## Table of Contents
1. [Key Features](#key-features)
2. [Technology Stack](#technology-stack)
3. [Architecture and Design Decisions](#architecture-and-design-decisions)
4. [Project Structure](#project-structure)
5. [Database Schema & Migrations](#database-schema--migrations)
6. [Seeded Sample Data](#seeded-sample-data)
7. [Business Rules & Pricing Engine](#business-rules--pricing-engine)
8. [Concurrency Safety & Atomic Inventory](#concurrency-safety--atomic-inventory)
9. [REST API Documentation & Endpoints](#rest-api-documentation--endpoints)
10. [Authentication & Authorization](#authentication--authorization)
11. [Environment Configuration](#environment-configuration)
12. [Local Setup & Execution](#local-setup--execution)
13. [Docker & Containerized Deployment](#docker--containerized-deployment)
14. [Testing Suite](#testing-suite)
15. [Continuous Integration (CI)](#continuous-integration-ci)
16. [Known Limitations & Out of Scope](#known-limitations--out-of-scope)

---

## Key Features

- **Layered Architecture:** Clear separation of concerns across controllers, services, repositories, domain entities, DTOs, mappers, and security components.
- **Atomic Inventory Control:** Concurrency-safe seat reservations using SQL conditional updates (`UPDATE flights SET available_seats = available_seats - ? WHERE id = ? AND available_seats >= ?`), eliminating race conditions and overselling.
- **Deterministic Fare Engine:** Pure `BigDecimal` monetary calculations (scale 2, `RoundingMode.HALF_UP`) for base fares, student discounts, promotional codes, dynamic taxes, and platform service fees.
- **Role-Based Security:** Spring Security 6 with stateless JWT authentication, discriminating between `ROLE_PASSENGER` and `ROLE_ADMIN`.
- **Booking Lifecycle Management:** Guaranteed idempotent cancellations, automatic seat inventory restoration, and ownership validation preventing passengers from viewing or cancelling other users' bookings.
- **Database Versioning:** Repeatable Flyway database migrations (`V1__init_schema.sql`, `V2__seed_flights_and_admin.sql`) with foreign key constraints, indexes, and database-level checks.
- **RESTful API Design:** Standard HTTP verbs, clear response statuses (`200`, `201`, `400`, `401`, `403`, `404`, `409`), structured error payloads, and input validation via Jakarta Bean Validation.
- **Interactive Documentation:** Springdoc OpenAPI 3.0 / Swagger UI with built-in JWT Bearer authorization support.
- **Containerization & CI:** Multi-stage production `Dockerfile`, `compose.yaml` with health checks, and GitHub Actions workflow.

---

## Technology Stack

| Layer / Concern | Technology | Version |
|---|---|---|
| Language | Java OpenJDK | 21 |
| Framework | Spring Boot | 3.3.4 |
| Security | Spring Security + JJWT | 6.x / 0.12.6 |
| Persistence | Spring Data JPA / Hibernate | 6.x |
| Database | PostgreSQL | 16 (or compatible) |
| Database Migration | Flyway Core + Flyway Database PostgreSQL | 10.x |
| Validation | Jakarta Bean Validation (Hibernate Validator) | 3.0 |
| API Docs | Springdoc OpenAPI UI | 2.6.0 |
| Testing | JUnit 5, Mockito, Spring Boot Test, MockMvc | Latest compatible |
| Build Tool | Apache Maven | 3.9+ |
| Containerization | Docker & Docker Compose | Compose V2 |
| CI / CD | GitHub Actions | Ubuntu Latest |

---

## Architecture and Design Decisions

The application adheres to a clean, maintainable **Layered Modular Monolith** architecture:

```
                  ┌──────────────────────────────────────────────┐
                  │              HTTP Clients / SPA              │
                  └──────────────────────┬───────────────────────┘
                                         │ JSON / HTTP
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │               Security Filters               │
                  │   (JwtAuthenticationFilter, SecurityConfig)   │
                  └──────────────────────┬───────────────────────┘
                                         │
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │               Controller Layer               │
                  │ (AuthController, FlightController, Admin,    │
                  │  BookingController, GlobalExceptionHandler)  │
                  └──────────────────────┬───────────────────────┘
                                         │ DTOs (Request / Response)
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │                Service Layer                 │
                  │ (AuthService, FlightService, BookingService, │
                  │  FareCalculationService, Mappers)            │
                  └──────────────────────┬───────────────────────┘
                                         │ Domain Entities
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │               Repository Layer               │
                  │ (UserRepository, FlightRepository,           │
                  │  BookingRepository)                          │
                  └──────────────────────┬───────────────────────┘
                                         │ SQL / Conditional Updates
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │             PostgreSQL Database              │
                  │  (Flyway Migrations, Constraints, Indexes)   │
                  └──────────────────────────────────────────────┘
```

### Key Design Principles:
1. **No JPA Entities in Controllers:** JPA entities are never exposed in REST API responses or accepted directly in request bodies. All contracts use immutable DTOs (`FlightResponse`, `BookingResponse`, etc.).
2. **Strict BigDecimal Calculations:** To prevent binary floating-point rounding errors in financial transactions, all calculations use `BigDecimal` with explicit scale and rounding mode.
3. **Pessimistic / Atomic Inventory Management:** Avoided standard in-memory read-modify-write patterns (`flight.setAvailableSeats(flight.getAvailableSeats() - seats)`) which fail under high concurrency. Utilized atomic SQL update statements that enforce inventory constraints directly at the database engine level.

---

## Project Structure

```text
flight-booking-system/
├── pom.xml
├── Dockerfile
├── compose.yaml
├── .gitignore
├── .env.example
├── README.md
├── .github/
│   └── workflows/
│       └── ci.yml
├── src/
│   ├── main/
│   │   ├── java/com/kiran/flightbooking/
│   │   │   ├── FlightBookingApplication.java
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminFlightController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BookingController.java
│   │   │   │   └── FlightController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── BookingCreateRequest.java
│   │   │   │   │   ├── FlightCreateRequest.java
│   │   │   │   │   ├── FlightStatusUpdateRequest.java
│   │   │   │   │   ├── FlightUpdateRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   └── RegisterRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ApiErrorResponse.java
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── BookingResponse.java
│   │   │   │       ├── FareBreakdownResponse.java
│   │   │   │       ├── FlightResponse.java
│   │   │   │       └── PageResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Booking.java
│   │   │   │   ├── Flight.java
│   │   │   │   └── User.java
│   │   │   ├── enums/
│   │   │   │   ├── BookingStatus.java
│   │   │   │   ├── FlightStatus.java
│   │   │   │   ├── PassengerType.java
│   │   │   │   └── Role.java
│   │   │   ├── exception/
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InsufficientSeatsException.java
│   │   │   │   ├── InvalidBookingStateException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UnauthorizedAccessException.java
│   │   │   ├── mapper/
│   │   │   │   ├── BookingMapper.java
│   │   │   │   └── FlightMapper.java
│   │   │   ├── repository/
│   │   │   │   ├── BookingRepository.java
│   │   │   │   ├── FlightRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── SecurityUtils.java
│   │   │   │   └── UserPrincipal.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── BookingService.java
│   │   │       ├── FareCalculationService.java
│   │   │       └── FlightService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__init_schema.sql
│   │           └── V2__seed_flights_and_admin.sql
│   └── test/
│       ├── java/com/kiran/flightbooking/
│       │   ├── controller/
│       │   │   ├── AuthControllerTest.java
│       │   │   └── FlightControllerTest.java
│       │   └── service/
│       │       ├── BookingServiceTest.java
│       │       ├── FareCalculationServiceTest.java
│       │       └── FlightServiceTest.java
│       └── resources/
│           └── application-test.yml
```

---

## Database Schema & Migrations

Flyway manages database migrations under `src/main/resources/db/migration/`:

### 1. `V1__init_schema.sql`
- **`users` Table:** Primary key `id`, normalized `email` (unique index), `password_hash`, `full_name`, `role` (`PASSENGER` / `ADMIN`), timestamps.
- **`flights` Table:** Primary key `id`, `flight_code` (unique index), `airline`, `source`, `destination`, `departure_time`, `arrival_time`, `total_seats`, `available_seats`, `base_fare`, `status` (`SCHEDULED`, `DELAYED`, `CANCELLED`, `COMPLETED`), check constraints (`chk_seats`: `available_seats >= 0 AND available_seats <= total_seats`, `chk_route`: `source <> destination`).
- **`bookings` Table:** Primary key `id`, `booking_reference` (unique index), foreign keys to `users(id)` and `flights(id)` with `ON DELETE RESTRICT` to protect audit history, `ticket_count`, `passenger_type` (`REGULAR`, `STUDENT`), itemized monetary fields (`subtotal`, `discount_amount`, `tax_amount`, `service_fee`, `total_amount`), `status` (`CONFIRMED`, `CANCELLED`), `cancelled_at`.

### 2. `V2__seed_flights_and_admin.sql`
- Idempotently seeds the 3 standard Kiran Academy flights:
  - `PN101`: Kiran Air (Pune to Delhi), 20 seats, Base Fare ₹4000.00
  - `MB202`: Sky Wings (Mumbai to Bengaluru), 30 seats, Base Fare ₹3500.00
  - `PN303`: Cloud Jet (Pune to Hyderabad), 25 seats, Base Fare ₹3000.00
- Idempotently seeds an initial Administrator account:
  - Email: `admin@flightbooking.com`
  - Password: `AdminPassword123` (BCrypt encoded)

---

## Business Rules & Pricing Engine

The fare calculation order is strictly defined and verified:

1. **Subtotal:**
   $$\text{Subtotal} = \text{baseFare} \times \text{ticketCount}$$
2. **Passenger Category Discount:**
   - `REGULAR`: 0% discount
   - `STUDENT`: 10% discount on Subtotal
3. **Promotional Discount:**
   - `FLYPROMO500`: Flat ₹500.00 off (capped at the discounted subtotal)
   - `SAVE10`: 10% additional discount on discounted subtotal
4. **Taxable Base:**
   $$\text{TaxableBase} = \max(0, \text{Subtotal} - \text{TotalDiscount})$$
5. **Government / Airport Tax:**
   $$\text{TaxAmount} = \text{TaxableBase} \times 0.05 \quad (\text{5\%})$$
6. **Platform Service Fee:**
   $$\text{ServiceFee} = ₹50.00 \quad (\text{flat per booking})$$
7. **Total Payable Amount:**
   $$\text{TotalAmount} = \text{TaxableBase} + \text{TaxAmount} + \text{ServiceFee}$$

All calculations use `BigDecimal.setScale(2, RoundingMode.HALF_UP)`.

### Cancellation & Refund Policy
- Passengers may cancel a confirmed booking only **before** the flight departure time.
- Cancellation fee is flat ₹100.00:
  $$\text{RefundAmount} = \max(0, \text{TotalAmountPaid} - ₹100.00)$$
- Repeated cancellation attempts on an already cancelled booking are rejected with `400 Bad Request`.
- Seat inventory is restored **exactly once** upon cancellation.

---

## Concurrency Safety & Atomic Inventory

To prevent race conditions where multiple simultaneous booking requests compete for remaining seats, inventory updates are performed atomically:

```sql
UPDATE flights
SET available_seats = available_seats - :seatsRequested,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :flightId
  AND available_seats >= :seatsRequested;
```

### Guarantees:
1. **Row-level locking in PostgreSQL:** The conditional `WHERE available_seats >= :seatsRequested` ensures the database engine serializes seat deductions.
2. **Affected row inspection:** If another transaction snatched the last seats, the query affects `0` rows.
3. **Instant failure & rollback:** `FlightRepository.reserveSeats(...)` returns `0`, causing `BookingService` to throw `InsufficientSeatsException` which aborts the transaction cleanly without leaving partial records.
4. **No negative inventory:** Database check constraint `CHECK (available_seats >= 0)` guarantees that available seats can never drop below zero under any circumstance.

---

## REST API Documentation & Endpoints

Base URL: `http://localhost:8080`

### Authentication Endpoints
| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register passenger account & get JWT token |
| `POST` | `/api/v1/auth/login` | Public | Authenticate credentials & get JWT token |

### Flight Endpoints
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/flights` | Public | Search bookable flights with filters & pagination |
| `GET` | `/api/v1/flights/{id}` | Public | Get flight details and remaining seats |
| `POST` | `/api/v1/admin/flights` | Admin | Create a new flight schedule |
| `PUT` | `/api/v1/admin/flights/{id}` | Admin | Update route, timings and base fare |
| `PATCH` | `/api/v1/admin/flights/{id}/status` | Admin | Change flight status (`SCHEDULED`, `DELAYED`, `CANCELLED`, `COMPLETED`) |
| `POST` | `/api/v1/admin/flights/{id}/cancel` | Admin | Cancel a flight schedule |

### Booking Endpoints
| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/bookings` | Passenger/Admin | Create booking and reserve seats atomically |
| `GET` | `/api/v1/bookings` | Passenger/Admin | View paginated booking history for current user |
| `GET` | `/api/v1/bookings/{bookingReference}` | Owner/Admin | View booking details with itemized fare breakdown |
| `POST` | `/api/v1/bookings/{bookingReference}/cancel` | Owner/Admin | Cancel booking, release seats, calculate refund |
| `GET` | `/api/v1/bookings/preview-fare` | Public | Preview fare calculation breakdown without booking |

### System & Health Endpoints
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/swagger-ui/index.html` | Public | Interactive Swagger UI API documentation |
| `GET` | `/v3/api-docs` | Public | OpenAPI 3.0 specification JSON |
| `GET` | `/actuator/health` | Public | Application and database readiness probe |

---

## Authentication & Authorization

All authenticated endpoints require an `Authorization` header containing the JWT token:

```http
Authorization: Bearer <your-jwt-token>
```

### Roles:
- **`ROLE_PASSENGER`**: Default role for registered users. Can search flights, preview pricing, create bookings, view their own bookings, and cancel their own bookings.
- **`ROLE_ADMIN`**: Can schedule flights, update flight timings, modify flight status, and inspect bookings.

### Ownership Security Rule:
Passengers cannot view or cancel other passengers' bookings. Attempting to access another user's booking reference returns `403 Forbidden` (`UnauthorizedAccessException`).

---

## Environment Configuration

Configuration is managed via `application.yml` and can be overridden with environment variables (e.g., in `.env` or system environment):

| Property / Environment Variable | Default Value | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/flight_booking_db` | PostgreSQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `APP_JWT_SECRET` | 256-bit default dev key | HMAC-SHA256 secret key for signing JWTs |
| `APP_JWT_EXPIRATION_MS` | `86400000` (24h) | Token lifespan in milliseconds |
| `APP_PRICING_TAX_RATE` | `0.05` (5%) | Configurable tax rate |
| `APP_PRICING_SERVICE_FEE` | `50.00` | Configurable service fee |
| `APP_PRICING_CANCELLATION_FEE`| `100.00` | Configurable cancellation fee |

See `.env.example` in the project root for reference.

---

## Local Setup & Execution

### Prerequisites:
- Java OpenJDK 21 installed (`java -version`)
- PostgreSQL 16+ running locally or in Docker
- Maven 3.9+ installed

### Step 1: Create Database
```sql
CREATE DATABASE flight_booking_db;
```

### Step 2: Configure Environment Variables
Copy `.env.example` to `.env` or export environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/flight_booking_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export APP_JWT_SECRET=super-secret-jwt-key-for-local-testing-purposes-must-be-long-enough-32bytes
```

### Step 3: Build and Run Application
```bash
mvn clean package -DskipTests
java -jar target/flight-booking-system-1.0.0.jar
```

Or using Spring Boot Maven plugin:
```bash
mvn spring-boot:run
```

Once running, access Swagger UI at:
👉 **`http://localhost:8080/swagger-ui/index.html`**

---

## Docker & Containerized Deployment

A multi-stage `Dockerfile` and `compose.yaml` are included for full containerization:

### Start Database and Application with Docker Compose:
```bash
docker compose up -d --build
```

### Check Logs & Status:
```bash
docker compose ps
docker compose logs -f app
```

### Stop Containers & Clean Up:
```bash
docker compose down
# Or to wipe database volume:
docker compose down -v
```

---

## Testing Suite

The project includes unit and integration tests:

### Running Tests:
```bash
mvn clean test
```

### Test Coverage Highlights:
- **`FareCalculationServiceTest`**: Validates regular vs. student discounts, 5% tax, flat service fees, promotional discount codes, boundary conditions (negative fares, zero tickets), and cancellation refund calculations.
- **`BookingServiceTest`**: Validates successful bookings, concurrency-safe inventory checks, insufficient seat exceptions, flight state validations (departed/cancelled), cancellation seat restoration, and ownership violation rejections.
- **`FlightServiceTest`**: Validates flight creation, duplicate flight code detection, identical source-destination prevention, and schedule ordering.
- **`AuthControllerTest` & `FlightControllerTest`**: MockMvc HTTP tests verifying status codes (`200 OK`, `201 Created`, `400 Bad Request`), JSON payloads, and validation error messages.

---

## Continuous Integration (CI)

A GitHub Actions workflow is located at `.github/workflows/ci.yml`.

### Workflow Stages:
1. Checks out repository code.
2. Sets up OpenJDK 21 with Maven dependency caching.
3. Compiles the Java application.
4. Executes unit and integration test suites.
5. Builds and packages the production executable JAR artifact.
6. Uploads the built artifact.

---

## Known Limitations & Out of Scope

- **Real Payment Processing:** Payment gateways (Razorpay, Stripe) are out of scope. Payment confirmation is simulated transactionally upon booking creation.
- **Seat Map / Specific Seat Numbers:** Version 1.0 supports aggregate seat inventory management. Individual seat numbers (e.g., 12A, 14C) are planned for Version 2.0.
- **Email / SMS Notifications:** Real email/SMS integrations are intentionally out of scope to avoid external third-party dependencies.
