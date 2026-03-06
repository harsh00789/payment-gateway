FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY payment/payment .

RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

EXPOSE 8081

CMD ["java","-jar","build/libs/*.jar"]