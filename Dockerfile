FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src/ src/
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S cotaseguro && adduser -S cotaseguro -G cotaseguro
COPY --from=build /workspace/target/*.jar app.jar
USER cotaseguro
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
