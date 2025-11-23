FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /build
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /build/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]