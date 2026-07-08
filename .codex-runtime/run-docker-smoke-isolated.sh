#!/usr/bin/env bash
set -euo pipefail

cd /mnt/d/ruoyi-vue-pro

tr -d '\r' < /mnt/c/Users/Administrator/.tester/skills/playwright/scripts/playwright_cli.sh > /tmp/pwcli.sh
bash /tmp/pwcli.sh --session docker-isolated-smoke open http://localhost:18080/login >/tmp/pw-isolated-open.log

CODE="$(python3 - <<'PY'
from pathlib import Path
code = Path(".tester-runtime/docker-smoke-pages.js").read_text(encoding="utf-8")
code = code.replace("http://localhost:8080", "http://localhost:18080")
code = code.replace("docker-smoke-", "docker-isolated-smoke-")
print(" ".join(code.splitlines()))
PY
)"

bash /tmp/pwcli.sh --session docker-isolated-smoke run-code "$CODE"
