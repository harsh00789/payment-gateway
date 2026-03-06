# ---------- Build Stage ----------
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy entire repo
COPY . .

# Move to the folder that contains build.gradle
WORKDIR /app/payment/payment

# Build jar
RUN gradle build -x test

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/payment/payment/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]