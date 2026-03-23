FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent pom
COPY pom.xml .

# Copy ALL modules (required for multi-module build)
COPY ecommerce-common-dtos ./ecommerce-common-dtos
COPY discovery-server ./discovery-server
COPY auth-service ./auth-service
COPY api-gateway ./api-gateway
COPY product-service ./product-service
COPY inventory-service ./inventory-service
COPY order-service ./order-service
COPY payment-service ./payment-service

# Build only auth-service + its dependencies
RUN mvn clean package -pl auth-service -am -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built jar
COPY --from=build /app/auth-service/target/*.jar app.jar

# Expose auth-service port
EXPOSE 8090

# Run with docker profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]