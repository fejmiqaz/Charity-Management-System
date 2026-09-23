# ============================================
# Stage 1: Build React frontend
# ============================================
FROM node:22-alpine AS frontend-build

WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./

RUN npm run build


# ============================================
# Stage 2: Build Spring Boot backend
# ============================================
FROM maven:3.9.9-eclipse-temurin-17 AS backend-build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

RUN chmod +x mvnw

# Copy compiled React application into Spring Boot static resources
COPY --from=frontend-build /frontend/dist ./src/main/resources/static

RUN ./mvnw clean package -DskipTests


# ============================================
# Stage 3: Production
# ============================================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


## OLD DOCKERFILE ONLY WITH STATIC TEMPLATES OF SPRING BOOT!!

## Build stage
#FROM maven:3.9.9-eclipse-temurin-17 AS build
#
#WORKDIR /app
#
#COPY pom.xml .
#COPY .mvn .mvn
#COPY mvnw .
#COPY src src
#
#RUN chmod +x mvnw
#RUN ./mvnw clean package -DskipTests
#
## Run stage
#FROM eclipse-temurin:17-jre
#
#WORKDIR /app
#
#COPY --from=build /app/target/*.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]