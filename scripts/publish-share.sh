#!/usr/bin/env bash
# 生成对外协作用的干净仓库镜像（剔除 AI 协作痕迹与内部文档）。
# 用法：bash scripts/publish-share.sh [源仓库路径] [目标目录]
# 默认：源=D:/ruoyi-vue-pro 目标=D:/ruoyi-vue-share
# 说明：每次全量重建镜像，原仓库只读不受影响；完成后自行 git push 到协作仓库。
set -euo pipefail

SRC="${1:-D:/ruoyi-vue-pro}"
DEST="${2:-D:/ruoyi-vue-share}"

BLACKLIST_PATHS=(
  AGENTS.md
  docs
  openspec
  .codex
  .codex-artifacts
  .codex-chrome-profile
  .codex-edge-profile
  .codex-edge-profile-2
  .continue
  .omo
  .opencode.disabled-20260626
  .scratch
  artifacts
  output
  scan_encoding.ps1
  "MES集成方式评估.md"
  "本周工作汇报.md"
  production/ruoyi-vue-pro/awesome-design-md-skill
  tools/mcp-package
  scripts/check-backend-patterns.ps1
)

# commit message 与文件内容中的痕迹改写规则（literal 匹配）
MESSAGE_RULES='literal:codex/==>feature/
literal:opencode/==>dev/
literal:codex==>专家'
CONTENT_RULES='literal:codex==>tester
literal:Codex==>Tester
literal:CODEX==>TESTER
literal:opencode==>tester
literal:OpenCode==>Tester
literal:OPENCODE==>TESTER'

command -v git >/dev/null || { echo "缺少 git"; exit 1; }
git filter-repo --version >/dev/null 2>&1 || { echo "缺少 git-filter-repo，先执行: pip install git-filter-repo"; exit 1; }

MSG_FILE="$(mktemp)"
TXT_FILE="$(mktemp)"
trap 'rm -f "$MSG_FILE" "$TXT_FILE"' EXIT
printf '%s\n' "$MESSAGE_RULES" > "$MSG_FILE"
printf '%s\n' "$CONTENT_RULES" > "$TXT_FILE"

rm -rf "$DEST"
git clone --no-local "$SRC" "$DEST"

PATH_ARGS=()
for p in "${BLACKLIST_PATHS[@]}"; do
  PATH_ARGS+=(--path "$p")
done

cd "$DEST"
git filter-repo --force --invert-paths "${PATH_ARGS[@]}" \
  --replace-message "$MSG_FILE" --replace-text "$TXT_FILE"

# 分支重命名：AI 前缀改为中性前缀，与改写后的历史 message 自洽
while read -r b; do
  new="${b/codex\//feature/}"
  new="${new/opencode\//dev/}"
  git branch -m "$b" "$new"
done < <(git branch --format='%(refname:short)' | grep -E '^(codex|opencode)/' || true)

# 单分支发布模式：只保留活跃开发线并作为 master 发布。
# 其余分支为一次性任务线（上机包/验收/迁移），普遍拖着 .opencode/ 等痕迹目录，禁止外发。
PUBLISH_SOURCE_BRANCH="${PUBLISH_SOURCE_BRANCH:-feature/backend-frontend-fusion}"
git checkout -q "$PUBLISH_SOURCE_BRANCH"
while read -r b; do
  [ "$b" = "$PUBLISH_SOURCE_BRANCH" ] || git branch -D "$b"
done < <(git branch --format='%(refname:short)')
if [ "$PUBLISH_SOURCE_BRANCH" != "master" ]; then
  git branch -m "$PUBLISH_SOURCE_BRANCH" master
fi
git tag | xargs -r git tag -d >/dev/null

echo ""
echo "===== 剔除完成，开始自检 ====="
FAIL=0

for p in "${BLACKLIST_PATHS[@]}"; do
  if git log --all --oneline -- "$p" | grep -q .; then
    echo "[失败] 历史中仍存在路径: $p"; FAIL=1
  fi
done
[ "$FAIL" -eq 0 ] && echo "[通过] 黑名单路径已从全部历史剔除"

if git log --all --format="%s %b" | grep -qiE "codex|opencode"; then
  echo "[失败] 历史 commit 信息仍含痕迹:"; git log --all --format="%h %s" | grep -iE "codex|opencode" | head -5; FAIL=1
else
  echo "[通过] 全部 commit 信息无 AI 工具痕迹"
fi

if git grep -iqE "codex|opencode" HEAD -- . ':!*.class'; then
  echo "[失败] 工作区文件仍含痕迹:"; git grep -ilE "codex|opencode" HEAD | head -5; FAIL=1
else
  echo "[通过] 工作区文件内容无 AI 工具痕迹"
fi

BAD_BRANCHES=$(git branch --format='%(refname:short)' | grep -icvE "^(master)$" || true)
if [ "$BAD_BRANCHES" -gt 0 ]; then
  echo "[失败] 发布分支之外仍有 $BAD_BRANCHES 个分支残留"; FAIL=1
else
  echo "[通过] 仅保留发布分支 master"
fi

TAG_COUNT=$(git tag | wc -l)
if [ "$TAG_COUNT" -gt 0 ]; then
  echo "[失败] 仍有 $TAG_COUNT 个 tag 未清除（tag 可能锚定脏历史）"; FAIL=1
else
  echo "[通过] tags 已全部移除"
fi

for keep in weitee-module-ai README.md LICENSE; do
  n=$(git ls-files -- "$keep" | wc -l)
  echo "[保留] $keep: $n 个文件"
done

[ "$FAIL" -eq 0 ] || { echo ""; echo "自检未通过，禁止推送"; exit 1; }

echo ""
echo "下一步（手动执行）："
echo "  cd $DEST"
echo "  git remote add origin <你的GitHub协作仓库地址>"
echo "  git push origin master        # 只推 master，禁止 push --all / --tags"
