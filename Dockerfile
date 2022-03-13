ARG JAVA_VERSION
FROM eclipse-temurin:${JAVA_VERSION} as builder

COPY [".", "fictional-meme/"]
# Clone and Build Jar
RUN cd fictional-meme && \
    chmod +x ./gradlew && \
    ./gradlew clean shadowJar

# ------------------------------------------------------------------
FROM eclipse-temurin:${JAVA_VERSION} as cache
ARG MINECRAFT_VERSION
COPY --from=builder /fictional-meme/app/build/libs/* /
COPY ["run/*", "gradlew", "/work/"]
RUN mkdir -p /work/gradle/wrapper && mv /work/*-wrapper.* /work/gradle/wrapper/

RUN echo $(java -jar $(find / -maxdepth 1 -name "*.jar") ${MINECRAFT_VERSION}) > /forge.txt
WORKDIR /work

RUN export CI_FORGE=$(cat /forge.txt) && \
    chmod +x ./gradlew && \
    (./gradlew prepareRuns --no-daemon > /dev/null || ./gradlew prepareRuns --no-daemon > /dev/null || \
     ./gradlew prepareRuns --no-daemon > /dev/null || (sleep 15s && ./gradlew prepareRuns --no-daemon)) && \
     ./gradlew build --no-daemon

RUN sed -i "s:mapping_channel=.*:mapping_channel=parchment:g" gradle.properties && \
    CI_FORGE=$(cat /forge.txt) PARCHMENT_ENABLED=true ./gradlew build --no-daemon

# ------------------------------------------------------------------
FROM eclipse-temurin:${JAVA_VERSION}

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --quiet git --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*
RUN mkdir -p /work/gradle/wrapper && mkdir -p /work/build/natives && mkdir -p /root/.gradle/caches
COPY --from=builder ["/fictional-meme/app/build/libs/*", "/fictional-meme/v-get/build/libs/*", "/"]
COPY --from=cache /root/.gradle/caches/ /root/.gradle/caches/
COPY --from=cache /work/build/natives/ /work/build/natives/
COPY ["run/*", "gradlew", "/work/"]
RUN mv /work/*-wrapper.* /work/gradle/wrapper/
