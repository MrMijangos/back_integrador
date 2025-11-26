FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .

RUN ./gradlew clean shadowJar -x test || ./gradlew clean build -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "app.jar"]
