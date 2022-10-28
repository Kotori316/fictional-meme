#!/usr/bin/env bash
set -eu

jar_path=$(find / -maxdepth 1 -name fictional-meme*.jar)
if [ $# -gt 0 ] ; then
  # One arg
  java -jar "${jar_path}" "$1"
else
  # No args
  java -jar "${jar_path}"
fi
