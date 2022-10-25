#!/usr/bin/env bash
set -eu
# Arg1: local location of gradle.properties
# Arg2: location of maven-metadata.xml

VERSION_GETTER_JAR=$(find / -maxdepth 1 -name "v-get*.jar")

MOD_VERSION=$(grep modVersion "$1")

java -jar "${VERSION_GETTER_JAR}" "${MOD_VERSION##*=}" "$2"
