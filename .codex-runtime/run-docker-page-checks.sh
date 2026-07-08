#!/usr/bin/env bash
set -euo pipefail

cd /mnt/d/ruoyi-vue-pro

tr -d '\r' < /mnt/c/Users/Administrator/.tester/skills/playwright/scripts/playwright_cli.sh > /tmp/pwcli.sh
bash /tmp/pwcli.sh --session docker-page-check open http://localhost:8080/login >/tmp/pw-page-check-open.log

CODE="$(python3 - <<'PY'
from pathlib import Path
print(" ".join(Path(".tester-runtime/docker-page-checks.js").read_text(encoding="utf-8").splitlines()))
PY
)"

bash /tmp/pwcli.sh --session docker-page-check run-code "$CODE" >/tmp/pw-page-check-run.log
bash /tmp/pwcli.sh --session docker-page-check eval "() => JSON.stringify(window.__dockerPageCheckResults)"
