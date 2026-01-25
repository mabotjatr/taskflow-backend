# -----------------------------
# Stage 1: Build the Quarkus app
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and resources (including keys in src/main/resources/keys/)
COPY src ./src

# Ensure keys are copied to the correct location
COPY src/main/resources/keys/ /app/src/main/resources/keys/

# Package the application into an uber-jar
RUN mvn clean package -DskipTests -Dquarkus.package.type=uber-jar

# -----------------------------
# Stage 2: Runtime image
# -----------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*-runner.jar /app/taskflow-backend.jar

# Copy keys to runtime image
COPY --from=build /app/src/main/resources/keys/ /app/keys/

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose Quarkus port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/taskflow-backend.jar"]
