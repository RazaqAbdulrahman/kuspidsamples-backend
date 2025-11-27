# ============================
# Build Stage
# ============================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ============================
# Run Stage
# ============================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose default port
EXPOSE 8080

# Default environment variables (dev-friendly)
ENV SPRING_PROFILES_ACTIVE=dev
ENV JWT_SECRET=""           # Dev: auto-generated if empty
ENV JWT_EXPIRATION=3600000  # 1 hour default

# Example prod variables (override these at runtime)
# ENV DATABASE_URL="jdbc:postgresql://host:port/dbname"
# ENV DATABASE_USERNAME="username"
# ENV DATABASE_PASSWORD="password"
# ENV CLOUDINARY_CLOUD_NAME="..."
# ENV CLOUDINARY_API_KEY="..."
# ENV CLOUDINARY_API_SECRET="..."
# ENV FRONTEND_URL="https://kuspidsamples.onrender.com"
# ENV JWT_SECRET="a-very-strong-secret-at-least-32-chars"
# ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
