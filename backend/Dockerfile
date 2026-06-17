# ===================== STAGE 1: BUILD =====================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Cache dependency downloads — only re-runs when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================== STAGE 2: RUNTIME =====================
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S siems && adduser -S siems -G siems

WORKDIR /app

COPY --from=build /app/target/siems-backend.jar app.jar

RUN apk add --no-cache curl

USER siems

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
