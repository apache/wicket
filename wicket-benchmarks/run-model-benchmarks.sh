#!/usr/bin/env bash
#
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
# Runs the additional-models footprint and JMH benchmarks and prints the raw output.
#   bash run-model-benchmarks.sh          quick  (~4 min, 1 fork)
#   bash run-model-benchmarks.sh full     full rigor (~15 min, 3 forks)

# re-exec under bash when started with sh: dash has no pipefail
if [ -z "${BASH_VERSION:-}" ]; then exec bash "$0" "$@"; fi

set -euo pipefail
cd "$(dirname "$(readlink -f "$0")")/.."

MODE="${1:-quick}"
OUT=target/model-benchmarks.txt
mkdir -p target

echo "### building (quiet) ..." >&2
mvn -o -q clean -pl wicket-core
mvn -o -q -pl wicket-benchmarks -am compile
# Copy the dependency jars into the module, rather than referencing ~/.m2: a confined
# process (Claude Code's snap) can read the repo but not hidden directories under $HOME.
if [ ! -d wicket-benchmarks/target/deps ]; then
  mvn -o -q -pl wicket-benchmarks dependency:copy-dependencies \
    -DoutputDirectory=wicket-benchmarks/target/deps \
  || mvn -q -pl wicket-benchmarks dependency:copy-dependencies \
    -DoutputDirectory=wicket-benchmarks/target/deps
fi

CP="wicket-benchmarks/target/classes:wicket-core/target/classes:wicket-util/target/classes:\
wicket-request/target/classes:wicket-tester/target/classes:wicket-benchmarks/target/deps/*"

{
  echo "===== JOL FOOTPRINT, default headers ====="
  java --add-opens java.base/java.lang=ALL-UNNAMED -cp "$CP" \
    org.apache.wicket.benchmarks.AdditionalModelsFootprint

  echo
  echo "===== JOL FOOTPRINT, -XX:+UseCompactObjectHeaders ====="
  java -XX:+UseCompactObjectHeaders --add-opens java.base/java.lang=ALL-UNNAMED -cp "$CP" \
    org.apache.wicket.benchmarks.AdditionalModelsFootprint 2>&1 || \
    echo "(compact object headers not supported by this JDK)"

  echo
  echo "===== JMH, mode=$MODE ====="
  if [ "$MODE" = full ]; then
    java -cp "$CP" org.openjdk.jmh.Main AdditionalModelsBenchmark -prof gc -jvmArgs "-Xmx1g"
  else
    java -cp "$CP" org.openjdk.jmh.Main AdditionalModelsBenchmark \
      -prof gc -jvmArgs "-Xmx1g" -f 1 -wi 3 -i 3 -r 1 -w 1
  fi
} 2>&1 | tee "$OUT"

echo >&2
echo "### raw output also saved to $OUT" >&2
