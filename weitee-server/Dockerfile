FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# 复制整个仓库，保证 Maven 可以解析父 pom 和聚合模块
COPY . .

RUN mvn -pl weitee-server -am -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/weitee-server/target/weitee-server.jar app.jar

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV ARGS=""

EXPOSE 48080

CMD java ${JAVA_OPTS} -jar app.jar $ARGS
