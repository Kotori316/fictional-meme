# syntax = docker/dockerfile:1

ARG JAVA_VERSION
FROM eclipse-temurin:${JAVA_VERSION} as builder

COPY ["app", "fictional-meme/app"]
COPY ["v-get", "fictional-meme/v-get"]
COPY ["gradle", "fictional-meme/gradle"]
COPY ["gradlew", "settings.gradle", "fictional-meme/"]

# Clone and Build Jar
RUN <<EOF
cd fictional-meme
chmod +x ./gradlew
./gradlew clean shadowJar
EOF

# ------------------------------------------------------------------
FROM eclipse-temurin:${JAVA_VERSION} as cache
ARG MINECRAFT_VERSION
ARG MAPPING_CHANNEL="official"
ARG MAPPING_VERSION=$MINECRAFT_VERSION
ARG PARCHMENT_MINECRAFT_VERSION=$MINECRAFT_VERSION
ARG REFERENCE_REPOSITORY
ARG REFERENCE_REPOSITORY_BRANCH
ARG LATEST_FORGE
RUN <<EOF
apt-get update
DEBIAN_FRONTEND=noninteractive apt-get install -y --quiet curl libxml2-utils git
apt-get clean
rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*
EOF
RUN <<EOF
curl -LSs -o ${PARCHMENT_MINECRAFT_VERSION}-versions.xml https://ldtteam.jfrog.io/artifactory/parchmentmc-public/org/parchmentmc/data/parchment-${PARCHMENT_MINECRAFT_VERSION}/maven-metadata.xml
xmllint -xpath "/metadata/versioning/release/text()" ${PARCHMENT_MINECRAFT_VERSION}-versions.xml > /parchment_version.txt
EOF
COPY ["run/build.gradle", "gradlew", "/work/"]
COPY ["gradle/wrapper/*", "/work/gradle/wrapper/"]

RUN echo "${LATEST_FORGE}" > /forge.txt
WORKDIR /work
RUN chmod +x ./gradlew

RUN CI_FORGE=$(cat /forge.txt) \
    MAPPING_CHANNEL=${MAPPING_CHANNEL} \
    MAPPING_VERSION=${MAPPING_VERSION} \
    ./gradlew prepareRuns build --no-daemon

RUN CI_FORGE=$(cat /forge.txt) \
    MAPPING_CHANNEL="parchment" \
    MAPPING_VERSION="${PARCHMENT_MINECRAFT_VERSION}-$(cat /parchment_version.txt)-${MINECRAFT_VERSION}" \
    ./gradlew build --no-daemon

RUN <<EOF
export FORGE_ONLY=true
if [ -n "${REFERENCE_REPOSITORY}" ] && [ -n "${REFERENCE_REPOSITORY_BRANCH}" ]; then
  git clone "${REFERENCE_REPOSITORY}" -b "${REFERENCE_REPOSITORY_BRANCH}" reference-repo
  cd reference-repo
  chmod +x ./gradlew
  ./gradlew build --no-daemon
fi
EOF

# ------------------------------------------------------------------
FROM eclipse-temurin:${JAVA_VERSION}

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --quiet git --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*
RUN mkdir -p /work/gradle/wrapper && mkdir -p /work/build/natives && mkdir -p /root/.gradle/caches

COPY --chmod=755 ["scripts/*.sh",  "/"]
COPY --from=cache /root/.gradle/caches/ /root/.gradle/caches/
COPY --from=cache /work/build/natives/ /work/build/natives/
COPY --from=cache /forge.txt /forge.txt
COPY --from=cache /parchment_version.txt /parchment_version.txt
COPY --from=builder ["/fictional-meme/app/build/libs/*", "/fictional-meme/v-get/build/libs/*", "/"]
