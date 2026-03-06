FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./gradlew build || gradle build

EXPOSE 8081

CMD ["java", "-jar", "build/libs/payment-0.0.1-SNAPSHOT.jar"]