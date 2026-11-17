#!/usr/bin/env bash
# 레포 설정을 한 번에 적용해요.
#
#   sh scripts/setup-github.sh                    처음 한 번
#       병합 방식, 라벨, 마일스톤, 브랜치 규칙, Actions 권한, 보안 기능
#   sh scripts/setup-github.sh require-ci <잡 이름>  CI가 한 번 돈 뒤
#       그 CI 잡 통과를 병합 필수 조건으로 추가
#
# 준비: gh auth login, 레포 루트에서 실행, master에 첫 커밋(.github 포함)이 올라가 있어야 해요.
set -euo pipefail

RULESET_NAME="protect-default-branch"
warn() { echo "  ! $*"; }

REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)

# 기본 브랜치 보호 규칙. $1에 CI 잡 이름을 주면 '통과 필수' 규칙이 추가돼요.
ruleset_json() {
  local checks=""
  if [ -n "${1:-}" ]; then
    checks=",
    {\"type\": \"required_status_checks\", \"parameters\": {
      \"strict_required_status_checks_policy\": false,
      \"required_status_checks\": [{\"context\": \"$1\"}]
    }}"
  fi
  cat <<EOF
{
  "name": "$RULESET_NAME",
  "target": "branch",
  "enforcement": "active",
  "conditions": {"ref_name": {"include": ["~DEFAULT_BRANCH"], "exclude": []}},
  "rules": [
    {"type": "deletion"},
    {"type": "non_fast_forward"},
    {"type": "required_linear_history"},
    {"type": "pull_request", "parameters": {
      "required_approving_review_count": 0,
      "dismiss_stale_reviews_on_push": false,
      "require_code_owner_review": false,
      "require_last_push_approval": false,
      "required_review_thread_resolution": true
    }}$checks
  ]
}
EOF
}

ruleset_id() {
  gh api "repos/$REPO/rulesets" --jq ".[] | select(.name == \"$RULESET_NAME\") | .id"
}

# ── CI 필수 조건 추가 모드 ─────────────────────────────
if [ "${1:-}" = "require-ci" ]; then
  CHECK="${2:?CI 잡 이름을 넣어 주세요. 예: sh scripts/setup-github.sh require-ci build}"
  ID=$(ruleset_id)
  if [ -z "$ID" ]; then
    echo "브랜치 규칙이 아직 없어요. 먼저 인자 없이 실행해 주세요."
    exit 1
  fi
  ruleset_json "$CHECK" | gh api -X PUT "repos/$REPO/rulesets/$ID" --input - > /dev/null
  echo "CI 잡 '$CHECK' 통과를 병합 필수 조건으로 추가했어요."
  exit 0
fi

# ── 처음 설정 ─────────────────────────────────────────
DEFAULT_BRANCH=$(gh repo view --json defaultBranchRef -q '.defaultBranchRef.name // ""')
VISIBILITY=$(gh repo view --json visibility -q .visibility)
echo "레포: $REPO / 공개 여부: $VISIBILITY / 기본 브랜치: ${DEFAULT_BRANCH:-없음}"

if [ -z "$DEFAULT_BRANCH" ]; then
  echo "레포가 비어 있어요. master에 첫 커밋(.github 폴더 포함)을 push한 뒤 다시 실행해 주세요."
  exit 1
fi

if [ "$DEFAULT_BRANCH" != "master" ]; then
  if gh api "repos/$REPO/branches/master" > /dev/null 2>&1; then
    gh repo edit "$REPO" --default-branch master
    echo "기본 브랜치를 master로 바꿨어요."
  else
    warn "원격에 master 브랜치가 없어요. master를 push한 뒤 다시 실행해 주세요."
    exit 1
  fi
fi

if grep -q "OWNER/REPO" .github/ISSUE_TEMPLATE/config.yml 2>/dev/null; then
  warn ".github/ISSUE_TEMPLATE/config.yml의 OWNER/REPO를 $REPO 로 바꾸고 PR로 반영해 주세요."
fi

# 1) 병합 방식: squash만, PR 제목을 커밋 제목으로, 병합 뒤 브랜치 삭제
gh repo edit "$REPO" \
  --enable-squash-merge \
  --enable-merge-commit=false \
  --enable-rebase-merge=false \
  --delete-branch-on-merge
gh api -X PATCH "repos/$REPO" \
  -f squash_merge_commit_title=PR_TITLE \
  -f squash_merge_commit_message=PR_BODY > /dev/null
echo "[1/6] 병합 방식"

# 2) 라벨 (이미 있으면 덮어써요)
label() { gh label create "$1" --color "$2" --description "$3" --force --repo "$REPO" > /dev/null; }
label "type: feature"    "a2eeef" "기능 추가·변경"
label "type: bug"        "d73a4a" "기대와 다르게 동작"
label "type: experiment" "5319e7" "측정해서 비교하는 일"
label "type: chore"      "cfd3d7" "잡일, 설정"
label "type: docs"       "0075ca" "문서, ADR"
label "adr"              "fbca04" "결정 기록이 필요함"
for area in auth study webhook judging notification ledger infra frontend; do
  label "area: $area" "c5def5" "영역: $area"
done
echo "[2/6] 라벨"

# 3) 마일스톤 (계획 12장 기준)
milestone() {
  gh api "repos/$REPO/milestones" -f title="$1" -f due_on="$2" -f description="$3" > /dev/null 2>&1 \
    || warn "마일스톤 건너뜀 (이미 있을 수 있음): $1"
}
milestone "P1 첫 운영 루프"  "2026-12-28T12:00:00Z" "본인 스터디가 자동 인증 + 알림으로 7일 연속 운영"
milestone "P2 신뢰성과 정산" "2027-01-25T12:00:00Z" "웹훅 누락 복구, 원장과 주간 정산"
milestone "P3 유지 기능"     "2027-02-15T12:00:00Z" "기수 이어가기, 통계"
echo "[3/6] 마일스톤"

# 4) 기본 브랜치 보호 규칙
if [ "$VISIBILITY" != "PUBLIC" ]; then
  warn "비공개 레포는 GitHub Free에서 브랜치 규칙을 쓸 수 없어요. 공개로 바꾼 뒤 다시 실행해 주세요."
elif [ -n "$(ruleset_id)" ]; then
  warn "브랜치 규칙 '$RULESET_NAME'이 이미 있어서 건너뛰어요."
else
  ruleset_json | gh api -X POST "repos/$REPO/rulesets" --input - > /dev/null
fi
echo "[4/6] 브랜치 규칙 (삭제·강제 push 금지, 선형 이력, PR 필수)"

# 5) Actions: 기본 토큰은 읽기 전용, Actions가 PR을 승인하지 못하게
gh api -X PUT "repos/$REPO/actions/permissions/workflow" \
  -f default_workflow_permissions=read \
  -F can_approve_pull_request_reviews=false > /dev/null
echo "[5/6] Actions 권한"

# 6) 보안 기능
gh api -X PUT "repos/$REPO/vulnerability-alerts" > /dev/null 2>&1 \
  || warn "Dependabot alerts를 켜지 못했어요. Settings → Code security에서 켜 주세요."
gh api -X PUT "repos/$REPO/automated-security-fixes" > /dev/null 2>&1 \
  || warn "Dependabot security updates를 켜지 못했어요. Settings → Code security에서 켜 주세요."
gh api -X PUT "repos/$REPO/private-vulnerability-reporting" > /dev/null 2>&1 \
  || warn "비공개 취약점 제보를 켜지 못했어요. Settings → Code security에서 켜 주세요."
gh api -X PATCH "repos/$REPO/code-scanning/default-setup" -f state=configured > /dev/null 2>&1 \
  || warn "CodeQL은 Java 코드가 올라간 뒤에 켤 수 있어요. 그때 다시 실행하거나 Settings → Code security에서 켜 주세요."
PUSH_PROTECTION=$(gh api "repos/$REPO" --jq '.security_and_analysis.secret_scanning_push_protection.status // "확인 불가"')
echo "[6/6] 보안 기능 (비밀값 푸시 보호: $PUSH_PROTECTION)"

echo
echo "남은 일: 첫 CI가 돈 뒤  sh scripts/setup-github.sh require-ci <잡 이름>"
