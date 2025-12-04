
# Payment Service (payment-srvc)

## Project Description

**payment-srvc** is a microservice responsible for managing payments related to events in the Kids Portal system.  
It handles creating and updating payments, tracking payment status, and retrieving payments by event or user.

This service is designed to work together with the main application **kids-portal** via a Feign client.

---

## Tech Stack

The following technologies and tools are used:

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL / H2 (for tests)
- JUnit 5 & Mockito (testing)
- MockMvc (API testing)
- Maven
- IntelliJ IDEA

---

## Architecture

This is a standalone microservice that exposes REST endpoints and communicates with the **kids-portal** main application.

Kids-portal uses a **FeignClient** to call this service.

Default port (local): **8084**

Base URL:
```
http://localhost:8084/api/v1/payments
```

---

## API Endpoints

### 1. Create / Update Payment

```
POST /api/v1/payments
```

Request body:
```json
{
  "eventId": "UUID",
  "userId": "UUID",
  "username": "string",
  "amount": 15,
  "type": "SINGLE",
  "status": "PENDING"
}
```

Response:  
`201 CREATED` with `PaymentResponse`

---

### 2. Update Payment Status (Toggle)

```
PUT /api/v1/payments/{eventId}/{userId}/status
```

Response:
- Returns updated `PaymentResponse`
- Toggles between `PENDING` and `PAID`

---

### 3. Get Payments by Event

```
GET /api/v1/payments/event/{eventId}
```

Returns:
```
List<PaymentResponse>
```

---

### 4. Get Payments by User

```
GET /api/v1/payments/user/{userId}
```

Returns:
```
List<PaymentResponse>
```

---

## Database Configuration

In `application.properties`:

```
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/payment_srvc
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

For testing, an in-memory H2 database is used.

---

## How to Run the Project

1. Make sure MySQL is running locally
2. Create the database:
   ```
   payment_srvc
   ```
   (Or let Hibernate create it automatically)
3. Run the application:

   ```
   mvn spring-boot:run
   ```

4. The service will start at:

   ```
   http://localhost:8084
   ```

---

## Testing

This project contains:

- Unit tests (Mockito)
- API tests (MockMvc)
- Integration tests (SpringBootTest with H2)

To run tests:

```
mvn test
```

Main test class:
```
PaymentITest.java
```

---

## Integration With Kids Portal

The **kids-portal** project uses this Feign client:

```java
@FeignClient(name = "payment", url = "http://localhost:8084/api/v1")
public interface PaymentClient {

    @PostMapping("/payments")
    ResponseEntity<Void> upsertPayment(@RequestBody PaymentRequest requestBody);

    @PutMapping("/payments/{eventId}/{userId}/status")
    PaymentResponse updateStatus(@PathVariable UUID eventId, @PathVariable UUID userId);

    @GetMapping("/payments/event/{eventId}")
    ResponseEntity<List<PaymentResponse>> getPaymentsByEventId(@PathVariable UUID eventId);

    @GetMapping("/payments/user/{userId}")
    ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable UUID userId);
}
```

---

## Future Improvements

- Add email notification when payment is completed
- Add Stripe/PayPal integration
- Add refund functionality
- Add admin UI for payments
- Add pagination & filtering

---

## Author

Developed as part of the **Kids Portal** ecosystem.  
This service is built for managing and tracking event payments efficiently and securely.
