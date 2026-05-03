FROM maven:3.9-eclipse-temurin-17 AS dev

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

EXPOSE 8080 35729

CMD ["mvn", "-B", "spring-boot:run", \
     "-Dspring-boot.run.fork=false", \
     "-Dspring-boot.run.profiles=docker"]
