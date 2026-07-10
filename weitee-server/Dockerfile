FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG ALL_PROXY
ARG NO_PROXY
ARG MAVEN_MIRROR=https://maven.aliyun.com/repository/public

ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTPS_PROXY}
ENV ALL_PROXY=${ALL_PROXY}
ENV NO_PROXY=${NO_PROXY}

# 复制整个仓库，保证 Maven 可以解析父 pom 和聚合模块
COPY . .

RUN mkdir -p /root/.m2 \
    && printf '%s\n' \
    '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' \
    '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' \
    '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' \
    '  <mirrors>' \
    '    <mirror>' \
    '      <id>custom-mirror</id>' \
    '      <name>custom-mirror</name>' \
    "      <url>${MAVEN_MIRROR}</url>" \
    '      <mirrorOf>*</mirrorOf>' \
    '    </mirror>' \
    '  </mirrors>' \
    '</settings>' > /root/.m2/settings.xml \
    && mvn -s /root/.m2/settings.xml -pl weitee-server -am -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/weitee-server/target/weitee-server.jar app.jar

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV ARGS=""

EXPOSE 48080

CMD java ${JAVA_OPTS} -jar app.jar $ARGS
