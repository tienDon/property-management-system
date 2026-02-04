# Property Management System (PMS)

This project is a comprehensive web application built with Spring Boot and Thymeleaf, designed to support the management, leasing, and operation of real estate properties (boarding houses, apartments, houses).

## Introduction

The system provides a platform connecting Owners and tenants, while offering convenient tools for managing contracts, services, and payments.

### Technologies Used

- **Backend:** Java (Spring Boot Framework)
- **Frontend:** Thymeleaf (Server-side rendering), Tailwind CSS, JavaScript, FontAwesome Icons
- **Database:** Microsoft SQL Server
- **Build Tool:** Maven
- **Payment:** VNPay Integration

---

## Features & Benefits

### 1. User Roles

The system supports the following main roles:

- **Guest (Public):** View property listings, search, and view details.
- **Owner:** Manage all properties and leasing activities.
- **Tenant:** Manage rental information(Will be added later).
- **Admin:** (Will be added later).

### 2. Landlord (Owner) Features

- **Dashboard:** Overview statistics of business performance.
- **Property Management:** Add, edit, delete information for houses/rooms.
- **Contract Management:** Create and manage lease agreements.
- **Service Management:** Configure accompanying services (electricity, water, internet, cleaning...).
- **Tenant Management:** Manage tenant information.
- **Posting Packages:** Purchase packages to list rooms, payment via VNPay.

### 3. Public Features

- Search for boarding houses/apartments with filters.
- View detailed information, images, amenities, and prices.

### 4. Data Initialization

The project comes with a built-in **Data Initialization** mechanism.

- When running the application for the first time, the system automatically loads sample data from `com.pms.propertymanagement.config.init.DataInitializer`.
- This data includes: Sample accounts, initial configurations, or necessary categories so you can experience the app immediately without manual data entry.

---

## Prerequisites

Before running the project, ensure your computer has the following installed:

1. **Java Development Kit (JDK):** Recommended version compatible with `pom.xml` (project is configured for Java 25, but can run with recent JDK versions).
2. **Maven:** For dependency management and building the project.
3. **Microsoft SQL Server:** The primary database.

---

## Installation and Run Guide

### Step 1: Clone the Project

```bash
git clone <your-repo-url>
cd "Property Management System"
```

### Step 2: Database Configuration

1. Create a new database in SQL Server named `PropertyManagementDB` (or any name you prefer).
2. Open `src/main/resources/application.properties`.
3. Update the database connection information to match your environment:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=PropertyManagementDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=<YOUR_USERNAME>  # e.g., sa
spring.datasource.password=<YOUR_PASSWORD>  # e.g., 123456
```

### Step 3: Run the Application

Open a terminal at the project root and run:

```bash
mvn spring-boot:run
```

_The first run may take some time to download Maven dependencies._

### Step 4: Access the Application

Once the application starts successfully (Console shows "Started..."), open your browser:

- **Home:** [http://localhost:8080](http://localhost:8080)
- **Owner Login:** [http://localhost:8080/login/owner](http://localhost:8080/login/owner)
  - **Username:** `owner1`
  - **Password:** `123`
  - _Use this account to access the Dashboard to test "Post Properties" features._

---

## Project Structure

```
src/main/java/com/pms/propertymanagement
├── config/          # Configuration 
├── controller/      # Controllers (Owner, Public, Auth...)
├── entity/          # JPA Entities
├── repository/      # JPA Repositories
├── service/         # Business Logic
└── PropertyManagementSystemApplication.java  # Main class
```

## Notes

- **Default Port:** 8080.
- **Sample Accounts:** Check `DataInitializer.java` or the console logs at startup for login credentials (if logged).

Hope you have a great experience with this Property Management System!
