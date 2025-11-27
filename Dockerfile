# ============================
# Build Stage (Optimized)
# ============================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml to cache Maven dependencies
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the actual application JAR
RUN mvn clean package -DskipTests


# ============================
# Run Stage (Lightweight)
# ============================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy only the built jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
