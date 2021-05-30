FROM adoptopenjdk:11-hotspot as builder

COPY [".", "fictional-meme/"]
# Clone and Build Jar
RUN cd fictional-meme && \
    chmod +x ./gradlew && \
    ./gradlew clean shadowJar

FROM adoptopenjdk:11-hotspot

COPY --from=builder /fictional-meme/app/build/libs/* /
COPY ["run/*", "gradlew", "gradle", "/work/"]
RUN mkdir /work/gradle && mv /work/wrapper/ /work/gradle/
