# ============================
# Single Dockerfile for Dev & Prod
# ============================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build jar
COPY src ./src
RUN mvn clean package -DskipTests

# ============================
# Run stage
# ============================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Default exposed port
EXPOSE 8080

# Default environment variables
ENV SPRING_PROFILES_ACTIVE=dev
ENV JWT_SECRET=""
       # Dev: auto-generated if empty
ENV JWT_EXPIRATION=3600000

# Example prod vars (override at runtime)
# ENV DATABASE_URL="jdbc:postgresql://host:port/dbname"
# ENV DATABASE_USERNAME="username"
# ENV DATABASE_PASSWORD="password"

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
