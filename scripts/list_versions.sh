#!/usr/bin/env bash
set -eu
# Arg1: location of maven-metadata.xml
# Arg2: search query(optional)

VERSION_GETTER_JAR=$(find / -maxdepth 1 -name "v-get*.jar")
location="$1"
if [ $# -gt 1 ] ; then
  query="$2"
else
  query=""
fi

java -cp "${VERSION_GETTER_JAR}" com.kotori316.maven.CountVersion "${location}" ${query}
