FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Принимаем креды как аргументы сборки
ARG GITHUB_USER
ARG GITHUB_TOKEN

COPY pom.xml .

# Генерируем settings.xml с репозиторием и токеном
RUN mkdir -p /root/.m2 && \
    echo '<settings> \
    <servers> \
        <server> \
            <id>github</id> \
            <username>'${GITHUB_USER}'</username> \
            <password>'${GITHUB_TOKEN}'</password> \
        </server> \
    </servers> \
    <profiles> \
        <profile> \
            <id>github-packages</id> \
            <activation><activeByDefault>true</activeByDefault></activation> \
            <repositories> \
                <repository> \
                    <id>github</id> \
                    <url>https://maven.pkg.github.com/BadzeiPavel/common-starter</url> \
                </repository> \
            </repositories> \
        </profile> \
    </profiles> \
</settings>' > /root/.m2/settings.xml

RUN mvn dependency:go-offline -B
COPY src/ src/
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jdk
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app

COPY --from=builder /build/target/authenticationservice-0.0.1-SNAPSHOT.jar authenticationservice.jar
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "authenticationservice.jar"]