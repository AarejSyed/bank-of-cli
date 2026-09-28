# Bank of CLI

## Overview

Bank of CLI’s Core Ledger is a prototype banking application that runs in the command terminal as a REPL program and provides:
- Account registration and login
- Account balance checking
- Bank transactions: deposit, withdrawal, and transfer
- Bank transaction history
- System logging

## Features

**Secure Access:** Users can register and log in using a unique Account ID and PIN.
**Balance Management:** Users can check their current account balance at any time.
**Transaction Engine:** Users can perform bank transactions:
- **Deposit:** Add funds to their account.
- **Withdraw:** Remove funds from their account with overdraft protection.
- **Transfer:** Transfer money from their account to another.
**Audit Trail:** Users can view their recent transaction history.
**System Logging:** The application maintains a log file to track activity, both successful and failed.

## Architecture

Core Ledger is a multi-layer application where each layer has different responsibilities:
- **API:** Displays information to the user and handles user input.
- **Service:** Handles business logic; the “brain” of the application.
- **Domain:** Provides abstraction over database tables and records.
- **Persistence:** Talks to the database and “translates” between Java and SQL.

Additionally, the application uses a PostgreSQL database.

## Technologies

**Java:** Programming language
**Maven:** Build tool
**PostgreSQL:** Database language
**JDBC:** Database communication
**JUnit 5 and Mockito:** Testing
**SLF4J/Logback:** Logging

## Project Structure

`/src/main/java/com/bankofcli/` contains the application's main Java codebase. Its subdirectories are:
- `api/`: Contains main class and front-end REPL.
- `service/`: Contains service layer interface and its implementation.
- `domain/`: Contains domain classes corresponding to accounts, bank transactions, and types of bank transactions.
- `persistence/`: Contains bank data access object interface and its implementation.

`/src/main/resources/` contains application resources such as `logback.xml` (logger configuration) and `db.properties` (database credentials).

`/src/test/` contains tests for the main codebase.

`/sql/` contains the project's SQL scripts.
- `schema.sql` sets up the Core Ledger database.
- `reset-local-db.sql` resets the database; this should only be used for local testing.

`/er_diagram/` contains past and current ER diagrams in XML and image formats.

`/presentation/` contains the presentation slide deck for this project in PowerPoint and PDF formats.

## Database Setup

For the application to connect to a PostgreSQL database, the user must create `db.properties` in `/src/main/resources/` according to this format:

```
DB_URL=jdbc:postgresql://localhost:5432/[NAME_OF_DATABASE]
DB_USER=[DATABASE_USERNAME]
DB_PASSWORD=[DATABASE_PASSWORD]
```

## Tests

Run `mvn test` in the terminal to run current unit tests.

## Logging

Logs are stored in `/logs/`, which is ignored.
