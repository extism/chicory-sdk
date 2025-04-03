#!/bin/bash
set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

docker run --rm \
    -v ${SCRIPT_DIR}/:/src \
    -w /src tinygo/tinygo:0.37.0 bash \
    -c "tinygo build -scheduler=none --no-debug -target=wasi -o /tmp/tmp.wasm main.go && cat /tmp/tmp.wasm" > \
    ${SCRIPT_DIR}/main.wasm
