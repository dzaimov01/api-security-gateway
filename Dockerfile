FROM maven:3.9.6-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY db ./db
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/api-security-gateway-0.1.0.jar app.jar
COPY config /config
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
