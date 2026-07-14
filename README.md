# Charity Management System

![Java](https://img.shields.io/badge/Java-23-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-336791)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED)
![Render](https://img.shields.io/badge/Render-Deployed-46E3B7)
![License](https://img.shields.io/badge/License-MIT-blue)

A full-stack **Charity Management System** built with **Spring Boot**, **Spring Security**, **Thymeleaf**, and **PostgreSQL** to streamline the management of charity organizations.

The system provides secure role-based access, complete CRUD functionality, advanced searching, filtering, pagination, PDF reporting, cloud deployment, and a responsive user interface for managing members, donations, events, projects, budgets, and yearly financial records.

---

# Features

## Authentication & Authorization

- Secure authentication with Spring Security
- BCrypt password encryption
- Role-based authorization
- Four permission levels:
  - HEAD
  - SUBHEAD
  - TREASURER
  - MEMBER
- Method-level security using `@PreAuthorize`
- Custom Access Denied page

---

## Dashboard

The dashboard provides an overview of the organization's data, including:

- Total Members
- Total Donations
- Total Projects
- Total Events
- Current Budget
- Quick navigation to every module

---

## Member Management

- Create, edit, view and delete members
- Assign organizational roles
- Link members to yearly records
- Search by name and email
- Filter by role, country and city
- Pagination

---

## Donation Management

- Create, edit and delete donations
- Assign multiple members
- Link donations to yearly records
- Search, filtering and pagination

---

## Event Management

- Create, edit and delete events
- Assign participating members
- Link events to yearly records
- Search, filtering and pagination

---

## Project Management

- Create, edit and delete projects
- Assign project members
- Track project status
- Link projects to yearly records
- Search, filtering and pagination

---

## Budget Management

- Manage yearly budgets
- Associate budgets with yearly records

---

## Year Management

- Create, edit and delete yearly records
- Connect budgets, members, donations, projects and events
- Search and pagination

---

# Additional Features

- PDF export functionality
- Global exception handling using `@ControllerAdvice`
- Application logging with **SLF4J** & **Logback**
- Server-side validation using Jakarta Validation
- Responsive Bootstrap 5 interface
- Dockerized application
- Cloud deployment on Render
- Neon PostgreSQL cloud database

---

# Technology Stack

## Backend

- Java 23
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- Spring Security
- Hibernate
- Maven

## Frontend

- Thymeleaf
- Bootstrap 5
- HTML5
- CSS3

## Database

- PostgreSQL
- Neon Cloud Database

## Deployment

- Docker
- Render
- GitHub

---

# Architecture

```text
Controller
     │
     ▼
Service
     │
     ▼
Repository
     │
     ▼
PostgreSQL
```

Additional architectural components include:

- DTO Layer
- Mapper Layer
- Validation
- Security
- Logging
- Global Exception Handling

---

# Project Structure

```text
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

---

# Skills Demonstrated

### Backend Development

- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- REST-oriented architecture principles
- Layered Architecture

### Database

- PostgreSQL
- Entity Relationships
- JPA/Hibernate ORM

### Application Design

- DTO Pattern
- Repository Pattern
- Service Layer Pattern
- Dependency Injection
- Role-Based Authorization
- Exception Handling
- Logging
- Validation

### Features

- CRUD Operations
- Pagination
- Filtering
- PDF Report Generation
- Responsive UI
- Docker Containerization
- Cloud Deployment

---

# Getting Started

## Clone the repository

```bash
git clone https://github.com/yourusername/Charity-Management-System.git
```

## Build the project

```bash
mvn clean install
```

## Run the application

```bash
mvn spring-boot:run
```

The application will be available at:

```
http://localhost:8080
```

Before running locally, configure your PostgreSQL connection inside:

```
src/main/resources/application.properties
```

---

# Future Improvements

- Excel Export
- REST API
- Email Notifications
- Dashboard Analytics
- Audit Logging
- Unit Tests
- Integration Tests

---

# Author

Developed as a full-stack Spring Boot application showcasing enterprise Java development with secure authentication, role-based authorization, layered architecture, cloud deployment, Docker, PostgreSQL, PDF reporting, logging, validation, pagination, filtering, and responsive web design.
