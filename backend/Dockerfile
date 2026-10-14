# ========================================
# Stage 1 — Build
# ========================================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Cache de dependências (só invalida se pom mudar)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compila
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ========================================
# Stage 2 — Runtime (JRE enxuto)
# ========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuário não-root
RUN addgroup -S app && adduser -S app -G app

# Copia o JAR
COPY --from=builder /app/target/questboard-backend-*.jar app.jar
RUN chown app:app app.jar
USER app

# Healthcheck usando o actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

EXPOSE 8080

# JVM flags otimizadas para container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]