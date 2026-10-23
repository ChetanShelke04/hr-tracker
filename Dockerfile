# Stage 1: Build the application from root
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
ENV MAVEN_OPTS="-Xmx384m -XX:+UseSerialGC"
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Xmx384m -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
