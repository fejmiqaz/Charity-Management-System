# Charity Management System

A full-stack **Charity Management System** built with **Spring Boot**,
**Thymeleaf**, **Spring Security**, and **PostgreSQL**, designed to
simplify the management of a charity organization's members, donations,
events, projects, budgets, and yearly financial records.

The application provides a secure role-based administration panel,
advanced search and pagination, PDF reporting, cloud deployment, and
comprehensive application logging.

------------------------------------------------------------------------

# Features

## Authentication & Authorization

-   Secure login system with Spring Security
-   Password encryption using BCrypt
-   Role-based access control
-   Four user roles:
    -   HEAD
    -   SUBHEAD
    -   TREASURER
    -   MEMBER
-   Method-level security using `@PreAuthorize`
-   Custom Access Denied page

## Dashboard

-   Organization overview
-   Total Members
-   Total Donations
-   Total Projects
-   Total Events
-   Current Budget
-   Quick navigation to all modules

## Member Management

-   Create, view, edit and delete members
-   Assign organizational roles
-   Assign members to yearly records
-   Search by name, surname and email
-   Filter by country, city and role
-   Pagination

## Donation Management

-   Create, edit and delete donations
-   Assign multiple members
-   Connect donations to yearly records
-   Search, filtering and pagination

## Event Management

-   Create, edit and delete events
-   Assign participating members
-   Connect events to yearly records
-   Search, filtering and pagination

## Project Management

-   Create, edit and delete projects
-   Assign project members
-   Track project status
-   Connect projects to yearly records
-   Search, filtering and pagination

## Budget Management

-   Create, edit and delete yearly budgets
-   Budget overview linked to yearly records

## Year Management

-   Create, edit and delete yearly records
-   Connect budgets, donations, events, members and projects
-   Search and pagination

------------------------------------------------------------------------

# Additional Features

-   PDF export for all major modules
-   Global exception handling using `@ControllerAdvice`
-   Application logging with **SLF4J** and **Logback**
-   Server-side validation using Jakarta Validation
-   Responsive UI with Bootstrap 5
-   Docker support
-   Cloud deployment on Render
-   Neon PostgreSQL cloud database

------------------------------------------------------------------------

# Technology Stack

## Backend

-   Java 23
-   Spring Boot 4
-   Spring MVC
-   Spring Data JPA
-   Spring Security
-   Hibernate
-   Maven

## Frontend

-   Thymeleaf
-   Bootstrap 5
-   HTML5
-   CSS3

## Database

-   PostgreSQL
-   Neon

## Deployment

-   Docker
-   Render
-   GitHub

------------------------------------------------------------------------

# Architecture

``` text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Additional layers include DTOs, Mappers, Security, Validation, Logging
and Exception Handling.

------------------------------------------------------------------------

# Project Structure

``` text
src
├── Config
├── DTO
├── Mapper
├── Models
├── Repository
├── Security
├── Service
│   └── Implementation
├── Web
├── templates
├── static
└── resources
```

------------------------------------------------------------------------

# Skills Demonstrated

-   Spring Boot
-   Spring Security
-   Spring Data JPA
-   Hibernate
-   PostgreSQL
-   Thymeleaf
-   Bootstrap
-   Docker
-   Cloud Deployment
-   Role-Based Authorization
-   CRUD Operations
-   DTO Pattern
-   Repository Pattern
-   Service Layer
-   Exception Handling
-   Logging
-   PDF Generation
-   Pagination
-   Filtering
-   Validation
-   Responsive Web Design

------------------------------------------------------------------------

# Getting Started

## Clone the repository

``` bash
git clone https://github.com/yourusername/Charity-Management-System.git
```

## Build

``` bash
mvn clean install
```

## Run

``` bash
mvn spring-boot:run
```

Open:

    http://localhost:8080

Update `application.properties` with your PostgreSQL configuration
before running.

------------------------------------------------------------------------

# Future Improvements

-   Excel Export
-   Audit Log
-   Email Notifications
-   REST API
-   Unit & Integration Tests
-   Dashboard Analytics

------------------------------------------------------------------------

# Author

Developed as a full-stack Spring Boot project demonstrating modern Java
enterprise application development using Spring Boot, Spring Security,
PostgreSQL, Thymeleaf, Docker, cloud deployment, logging, exception
handling, PDF generation, pagination and filtering.
