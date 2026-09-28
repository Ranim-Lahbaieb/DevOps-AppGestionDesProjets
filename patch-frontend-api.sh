#!/usr/bin/env bash
set -euo pipefail

DOSSIER="${1:-frontend/src/app/services}"

echo "==> Avant :"
grep -rn "private api" "$DOSSIER"

find "$DOSSIER" -name "*.ts" -exec \
    sed -i.bak "s|'http://localhost:8080\(/[a-z-]*\)'|'\1'|g" {} +

echo ""
echo "==> Apres :"
grep -rn "private api" "$DOSSIER" --include="*.ts"
