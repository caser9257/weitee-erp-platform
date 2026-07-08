#!/usr/bin/env bash
set -euo pipefail

cd /mnt/d/ruoyi-vue-pro

tr -d '\r' < /mnt/c/Users/Administrator/.tester/skills/playwright/scripts/playwright_cli.sh > /tmp/pwcli.sh
bash /tmp/pwcli.sh --session docker-approval-check open http://localhost:8080/login >/tmp/pw-approval-check-open.log

CODE="$(python3 - <<'PY'
from pathlib import Path
print(" ".join(Path(".tester-runtime/docker-approval-link-checks.js").read_text(encoding="utf-8").splitlines()))
PY
)"

bash /tmp/pwcli.sh --session docker-approval-check run-code "$CODE" >/tmp/pw-approval-check-run.log
bash /tmp/pwcli.sh --session docker-approval-check eval "() => JSON.stringify(window.__dockerApprovalLinkCheckResults)"
