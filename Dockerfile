FROM mcr.microsoft.com/openjdk/jdk:16-ubuntu as cache

ENV DEBIAN_FRONTEND=noninteractive
RUN sed -i -e 's%http://[^ ]\+%mirror://mirrors.ubuntu.com/mirrors.txt%g' /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        git \
        vim \
        && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

COPY ["gradlew", "gradle", "build.gradle", "settings.gradle", "/work/"]
RUN mkdir -p /work/gradle && mv /work/wrapper /work/gradle/wrapper/
RUN cd /work && chmod +x ./gradlew && \
    MINECRAFT_VERSION=1.17.1 ./gradlew build && \
    MINECRAFT_VERSION=1.17 ./gradlew build && \
    true

FROM mcr.microsoft.com/openjdk/jdk:16-ubuntu as last

RUN mkdir -p /root/.gradle/caches
COPY --from=cache /root/.gradle/caches/ /root/.gradle/caches/
