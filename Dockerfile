ARG JAVA_VERSION
FROM eclipse-temurin:${JAVA_VERSION} as builder

COPY ["app", "fictional-meme/app"]
COPY ["v-get", "fictional-meme/v-get"]
COPY ["gradle", "fictional-meme/gradle"]
COPY ["gradlew", "settings.gradle", "fictional-meme/"]

# Clone and Build Jar
RUN cd fictional-meme && \
    chmod +x ./gradlew && \
    ./gradlew clean shadowJar

# ------------------------------------------------------------------
FROM eclipse-temurin:${JAVA_VERSION} as cache
ARG MINECRAFT_VERSION
ARG MAPPING_CHANNEL="official"
ARG MAPPING_VERSION=$MINECRAFT_VERSION
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --quiet curl libxml2-utils --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*
RUN curl -Ss -o ${MINECRAFT_VERSION}-versions.xml https://ldtteam.jfrog.io/artifactory/parchmentmc-public/org/parchmentmc/data/parchment-${MINECRAFT_VERSION}/maven-metadata.xml && \
    xmllint -xpath "/metadata/versioning/release/text()" ${MINECRAFT_VERSION}-versions.xml > /parchment_version.txt
COPY ["run/build.gradle", "gradlew", "/work/"]
COPY ["gradle/wrapper/*", "/work/gradle/wrapper/"]

COPY --from=builder /fictional-meme/app/build/libs/* /

RUN echo $(java -jar $(find / -maxdepth 1 -name "*.jar") ${MINECRAFT_VERSION}) > /forge.txt
WORKDIR /work

RUN export CI_FORGE=$(cat /forge.txt) && \
    export MAPPING_CHANNEL=${MAPPING_CHANNEL} && \
    export MAPPING_VERSION=${MAPPING_VERSION} && \
    chmod +x ./gradlew && \
    (./gradlew prepareRuns --no-daemon > /dev/null || ./gradlew prepareRuns --no-daemon > /dev/null || \
     ./gradlew prepareRuns --no-daemon > /dev/null || (sleep 15s && ./gradlew prepareRuns --no-daemon)) && \
     ./gradlew build --no-daemon

RUN CI_FORGE=$(cat /forge.txt) MAPPING_CHANNEL="parchment" MAPPING_VERSION="${MINECRAFT_VERSION}-$(cat /parchment_version.txt)-${MINECRAFT_VERSION}" \
    ./gradlew build --no-daemon

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
COPY --from=cache /forge.txt /forge.txt
COPY --from=cache /parchment_version.txt /parchment_version.txt
