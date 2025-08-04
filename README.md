# Currency Exchange REST API Application

A simple application that serves a REST API, allowing users to create a currency account and exchange funds between PLN and USD.

## Technologies

* **Java 17**
* **Spring Boot 3**
* **Gradle**
* **JPA / Hibernate**
* **H2 Database (file-based)**
* **NBP API** (exchange rate provider)

## Features

1.  **Account Creation**: Create a new account by providing a first name, last name, and an initial balance in PLN.
2.  **Get Account Data**: Display account details, including balances for all held currencies.
3.  **Currency Exchange**: Perform an exchange of funds between PLN and USD at the current rate from the NBP API.

## Architecture

The application's architecture is based on the principles of **Domain-Driven Design (DDD)** and **Command Query Responsibility Segregation (CQRS)**.

* **DDD**: The core logic is encapsulated within the domain layer, using concepts like Aggregates (`Account`), Entities (`AccountWallet`), and domain services (`AccountFacade`). This approach ensures that the business logic is well-organized, maintainable, and isolated from infrastructure concerns.
* **CQRS**: The application separates the model for writing data (Commands, handled by the `Account` aggregate) from the model for reading data (Queries, handled by `AccountQuery` and `AccountQueryRepository`). This separation allows for optimizing each side independently—the write side for consistency and the read side for performance and flexibility.

## Testing

The project includes a comprehensive test suite to ensure code quality and correctness, divided into two main categories:

* **Facade Tests**: Located in `src/test`, these tests (`AccountFacadeSpec`) focus on the business logic within the domain facade. They use **in-memory repositories** to simulate the behavior of a real database, which makes them very fast and independent of an actual database instance. External dependencies (like the NBP API client) are mocked to ensure the tests run in complete isolation. They can be executed with the standard `./gradlew test` command.
* **Integration Tests**: Located in `src/integration`, these tests (`AccountControllerSpec`) verify the full application flow, from the HTTP API endpoints down to the database layer. They run against a real, albeit in-memory, database to ensure all layers work together correctly. They can be executed with `./gradlew integration` or as part of the full `./gradlew build` command.

---

## Requirements

* **JDK 17** or newer
* **Gradle** (optional, the project includes the Gradle Wrapper)

---

## Running the Application

1.  **Clone the repository**
    ```bash
    git clone <your-repository-address>
    cd currency-exchange
    ```
    *Note: On Unix/Linux/macOS systems, you may need to grant execution permissions to the Gradle Wrapper:*
    `chmod +x ./gradlew`

2.  **Build the application using Gradle**
    In the main project directory, run the command:
    ```bash
    ./gradlew build
    ```
    This will download all dependencies and build a `.jar` file in the `build/libs/` directory.

3.  **Run the application**
    ```bash
    java -jar build/libs/currency-exchange-0.0.1-SNAPSHOT.jar
    ```
    The application will start on port **8080**.

---

## API Documentation

### 1. Create a New Account

* **Endpoint**: `POST /api/accounts`
* **Description**: Creates a new user account.
* **Request Body**:
    ```json
    {
      "firstName": "Jan",
      "lastName": "Kowalski",
      "initialBalancePLN": 1500.50
    }
    ```
* **Example Response (201 Created)**:
    ```json
    {
      "accountId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "balances": [
        {
          "currencyCode": "PLN",
          "balance": 1500.50
        }
      ]
    }
    ```

### 2. Get Account Data

* **Endpoint**: `GET /api/accounts/{accountId}`
* **Description**: Returns information about an account based on its unique ID.
* **Example Usage (cURL)**:
    ```bash
    curl http://localhost:8080/api/accounts/a1b2c3d4-e5f6-7890-1234-567890abcdef
    ```
* **Example Response (200 OK)**:
    ```json
    {
      "accountId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "balances": [
        {
          "currencyCode": "PLN",
          "balance": 1500.50
        }
      ]
    }
    ```

### 3. Exchange Currency

* **Endpoint**: `POST /api/accounts/{accountId}/exchange`
* **Description**: Performs a currency exchange on a specific account.
* **Request Body**:
    ```json
    {
      "fromCurrency": "PLN",
      "toCurrency": "USD",
      "amount": 200.00
    }
    ```
* **Example Response (200 OK)** (assuming a rate of 1 USD = 4.00 PLN):
    ```json
    {
      "accountId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "firstName": "Jan",
      "lastName": "Kowalski",
      "balances": [
        {
          "currencyCode": "PLN",
          "balance": 1300.50
        },
        {
          "currencyCode": "USD",
          "balance": 50.00
        }
      ]
    }
    ```
