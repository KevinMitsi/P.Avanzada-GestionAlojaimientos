#
# Etapa de construcción
#
FROM gradle:8.7-jdk21 AS build
USER gradle
WORKDIR /home/gradle/project

# Copiar archivos de configuración de Gradle
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew ./

# Descargar dependencias (para cache)
RUN gradle --no-daemon dependencies || true

# Copiar el código fuente
COPY --chown=gradle:gradle src ./src

# Construir el JAR (sin tests para faster build)
RUN gradle --no-daemon clean bootJar -x test

# Verificar que el JAR existe
RUN ls -la /home/gradle/project/build/libs/

#
# Etapa de runtime
#
FROM eclipse-temurin:21-jre-alpine
ENV APP_HOME=/app
WORKDIR ${APP_HOME}

# Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde build stage
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Railway usa la variable PORT automáticamente
EXPOSE 8080

# Configuración de JVM optimizada para contenedores
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]