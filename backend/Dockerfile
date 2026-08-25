FROM maven:3.9-eclipse-temurin-21 AS build-base

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src

FROM build-base AS test
RUN mvn test -B

FROM build-base AS builder
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Shanghai
WORKDIR /app
RUN apk add --no-cache postgresql-client tzdata \
    && cp "/usr/share/zoneinfo/${TZ}" /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && addgroup -S appgroup \
    && adduser -S appuser -G appgroup \
    && mkdir -p /opt/tutor-assist/backups /opt/tutor-assist/materials \
    && chown -R appuser:appgroup /app /opt/tutor-assist

COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
