#
# ========== STAGE 1: BUILD ==========
#
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew build.gradle settings.gradle ./

RUN ./gradlew --no-daemon dependencies || true

COPY --chown=gradle:gradle src src

RUN ./gradlew --no-daemon clean bootJar -x test

#
# ========== STAGE 2: RUNTIME ==========
#
# ❗ Mejor sin Alpine: más estable en Railway
FROM eclipse-temurin:21-jre

ENV APP_HOME=/app
WORKDIR $APP_HOME

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
