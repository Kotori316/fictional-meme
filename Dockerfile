FROM adoptopenjdk:11-openj9-focal as builder

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y --no-install-recommends git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

# Clone and Build Jar
RUN git clone --depth 1 https://github.com/Kotori316/fictional-meme.git && \
    cd fictional-meme && \
    chmod +x ./gradlew && \
    ./gradlew shadowJar

FROM adoptopenjdk:11-hotspot

COPY --from=builder /fictional-meme/app/build/libs/* /
COPY ["run/*", "gradlew", "gradle", "/work/"]
RUN mkdir /work/gradle && mv /work/wrapper/ /work/gradle/
