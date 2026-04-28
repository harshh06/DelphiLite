#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: scripts/build-wasm.sh <input.ll> <output.wasm>"
  exit 1
fi

INPUT_LL="$1"
OUTPUT_WASM="$2"
OBJ_FILE="${OUTPUT_WASM%.wasm}.o"

if command -v llc >/dev/null 2>&1 && command -v wasm-ld >/dev/null 2>&1; then
  llc -mtriple=wasm32-unknown-unknown -filetype=obj -o "$OBJ_FILE" "$INPUT_LL"
  wasm-ld --no-entry --export-all --allow-undefined -o "$OUTPUT_WASM" "$OBJ_FILE"
  echo "Wrote $OUTPUT_WASM via llc + wasm-ld"
  exit 0
fi

if command -v clang >/dev/null 2>&1; then
  clang --target=wasm32-unknown-unknown -Wl,--no-entry -Wl,--export-all -Wl,--allow-undefined -nostdlib "$INPUT_LL" -o "$OUTPUT_WASM"
  echo "Wrote $OUTPUT_WASM via clang fallback"
  exit 0
fi

echo "Error: need llc+wasm-ld or clang with wasm32 target support."
exit 1
