# Étape 1 : build avec Maven + JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : image d'exécution (JRE 17 uniquement, plus légère)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/afristock-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]
