FROM adoptopenjdk:11-hotspot as builder

COPY [".", "fictional-meme/"]
# Clone and Build Jar
RUN cd fictional-meme && \
    chmod +x ./gradlew && \
    ./gradlew clean shadowJar

FROM adoptopenjdk:11-hotspot as cache
ARG MINECRAFT_VERSION=${MINECRAFT_VERSION:-1.16.5}
COPY --from=builder /fictional-meme/app/build/libs/* /
COPY ["run/*", "gradlew", "/work/"]
RUN mkdir -p /work/gradle/wrapper && mv /work/*-wrapper.* /work/gradle/wrapper/

RUN export CI_FORGE=$(java -jar $(find / -maxdepth 1 -name "*.jar") $MINECRAFT_VERSION) && \
    cd /work && \
    chmod +x ./gradlew && \
    (./gradlew downloadAssets --no-daemon > /dev/null || ./gradlew downloadAssets --no-daemon > /dev/null || \
     ./gradlew downloadAssets --no-daemon > /dev/null || (sleep 15s && ./gradlew downloadAssets --no-daemon)) && \
    (./gradlew extractNatives --no-daemon > /dev/null || ./gradlew extractNatives --no-daemon > /dev/null || \
     ./gradlew extractNatives --no-daemon > /dev/null || (sleep 15s && ./gradlew extractNatives --no-daemon)) && \
     ./gradlew runData --no-daemon

FROM adoptopenjdk:11-hotspot

RUN mkdir -p /work/gradle/wrapper && mkdir -p /work/build/natives && mkdir -p /root/.gradle/caches
COPY --from=builder /fictional-meme/app/build/libs/* /
COPY --from=cache /root/.gradle/caches/ /root/.gradle/caches/
COPY --from=cache /work/build/natives/ /work/build/natives/
COPY ["run/*", "gradlew", "/work/"]
RUN mv /work/*-wrapper.* /work/gradle/wrapper/
