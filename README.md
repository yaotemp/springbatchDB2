# Spring Batch MySQL to CSV Export

This Spring Boot application uses Spring Batch to export data from a MySQL database table to a CSV file.

## Prerequisites

- JDK 1.8
- Maven
- MySQL database

## Database Setup

The application expects a MySQL database with a table named `USERS` with the following structure:

```sql
CREATE TABLE USERS (
    `ID` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `AGE` INT NOT NULL,
    `BIRTHDAY` DATE,
    `COUNTRY` VARCHAR(255),
    `EMAIL` VARCHAR(255),
    `FIRST_NAME` VARCHAR(255),
    `GENDER` VARCHAR(255),
    `LAST_NAME` VARCHAR(255),
    `PERSON_ID` VARCHAR(255)
);
```

## Configuration

Update the database connection settings in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/batch_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Also, you can change the output CSV file path:

```properties
output.csv.path=./users_output.csv
```

## Running the Application

1. Build the application:
   ```
   mvn clean package
   ```

2. Run the application:
   ```
   java -jar target/springbatchmysql-0.0.1-SNAPSHOT.jar
   ```

The application will connect to the MySQL database, read all records from the USERS table, and write them to the specified CSV file.

## Project Structure

- `SpringBatchMysqlApplication.java`: Main application class
- `User.java`: Model class representing the USERS table
- `BatchConfig.java`: Spring Batch configuration with job, step, reader, and writer definitions 