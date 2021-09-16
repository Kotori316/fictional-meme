ARG JAVA_VERSION=${JAVA_VERSION:-11}
FROM eclipse-temurin:${JAVA_VERSION} as builder

COPY [".", "fictional-meme/"]
# Clone and Build Jar
RUN cd fictional-meme && \
    chmod +x ./gradlew && \
    ./gradlew clean shadowJar

FROM eclipse-temurin:${JAVA_VERSION} as cache
ARG MINECRAFT_VERSION=${MINECRAFT_VERSION:-1.16.5}
COPY --from=builder /fictional-meme/app/build/libs/* /
COPY ["run/*", "gradlew", "/work/"]
RUN mkdir -p /work/gradle/wrapper && mv /work/*-wrapper.* /work/gradle/wrapper/

RUN export CI_FORGE=$(java -jar $(find / -maxdepth 1 -name "*.jar") ${MINECRAFT_VERSION}-latest) && \
    cd /work && \
    chmod +x ./gradlew && \
    (./gradlew downloadAssets --no-daemon > /dev/null || ./gradlew downloadAssets --no-daemon > /dev/null || \
     ./gradlew downloadAssets --no-daemon > /dev/null || (sleep 15s && ./gradlew downloadAssets --no-daemon)) && \
    (./gradlew extractNatives --no-daemon > /dev/null || ./gradlew extractNatives --no-daemon > /dev/null || \
     ./gradlew extractNatives --no-daemon > /dev/null || (sleep 15s && ./gradlew extractNatives --no-daemon)) && \
     ./gradlew build --no-daemon
RUN export CI_FORGE=$(java -jar $(find / -maxdepth 1 -name "*.jar") ${MINECRAFT_VERSION}-recommended) && \
    cd /work && \
    chmod +x ./gradlew && \
    ./gradlew build --no-daemon

FROM eclipse-temurin:${JAVA_VERSION}

RUN DEBIAN_FRONTEND=noninteractive \
    && apt-get update \
    && apt-get install -y --quiet git --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*
RUN mkdir -p /work/gradle/wrapper && mkdir -p /work/build/natives && mkdir -p /root/.gradle/caches
COPY --from=builder /fictional-meme/app/build/libs/* /
COPY --from=cache /root/.gradle/caches/ /root/.gradle/caches/
COPY --from=cache /work/build/natives/ /work/build/natives/
COPY ["run/*", "gradlew", "/work/"]
RUN mv /work/*-wrapper.* /work/gradle/wrapper/
