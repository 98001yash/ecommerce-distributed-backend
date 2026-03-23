FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY ecommerce-common-dtos ./ecommerce-common-dtos
COPY discovery-server ./discovery-server
COPY auth-service ./auth-service
COPY api-gateway ./api-gateway
COPY product-service ./product-service
COPY inventory-service ./inventory-service
COPY order-service ./order-service
COPY payment-service ./payment-service

RUN mvn clean package -pl order-service -am -DskipTests


# ----------- RUNTIME STAGE -----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/order-service/target/*.jar app.jar

EXPOSE 8093

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]