#!/usr/bin/env bash
set -euo pipefail

# Run module-scoped test tasks in parallel to reduce overall CI time.
./gradlew :domain:test :data:test :presentation:test --parallel --build-cache "$@"
