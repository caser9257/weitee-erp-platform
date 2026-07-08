#!/usr/bin/env bash
set -euo pipefail

cd /mnt/d/ruoyi-vue-pro

tr -d '\r' < /mnt/c/Users/Administrator/.tester/skills/playwright/scripts/playwright_cli.sh > /tmp/pwcli.sh
bash /tmp/pwcli.sh --session docker-verify open http://localhost:8080/login >/tmp/pw-open.log

CODE="$(python3 - <<'PY'
from pathlib import Path
print(" ".join(Path(".tester-runtime/docker-smoke-pages.js").read_text(encoding="utf-8").splitlines()))
PY
)"
printf '%s' "$CODE" > /tmp/docker-smoke-pages.effective.js

bash /tmp/pwcli.sh --session docker-verify run-code "$CODE"
