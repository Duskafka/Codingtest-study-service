# 작업 방식

혼자 만드는 프로젝트지만 팀에서 일하는 방식대로 기록을 남겨요. 이슈, PR, 커밋이 그대로 "어떻게 생각하고 일했는지"의 증거가 돼요.

평일 2시간 안에서 돌아가야 하니까, 절차는 가볍게 두고 **결정과 검증만은 빠뜨리지 않는 것**을 원칙으로 해요.

## 1. 흐름 한눈에 보기

```
이슈 만들기 → 브랜치 → Draft PR → 작업 → Ready → 셀프 리뷰 (+ AI 리뷰) → squash 병합 → CI → GHCR → prod 배포
```

## 2. 브랜치

- 기본 브랜치는 `master` 하나만 길게 살아 있어요. `master`는 언제나 배포 가능한 상태예요 (GitHub Flow).
- `master`에는 직접 push할 수 없어요. 모든 변경은 PR로 들어가요.
- 작업 브랜치 이름: `<타입>/<이슈번호>-<짧은-설명>`
    - 예: `feat/12-webhook-signature`, `fix/31-deadline-timezone`
- 브랜치는 1~3세션(2~6시간) 안에 끝나는 크기로 만들어요. 넘으면 이슈를 쪼개요.

## 3. 커밋과 PR 제목

`master`에는 squash 병합만 해요. 그래서 **PR 제목이 곧 `master`의 커밋 메시지**가 돼요. PR 제목은 Conventional Commits 형식으로 써요.

```
<타입>(<범위>): <무엇을 했는지>
```

| 타입 | 언제 |
|---|---|
| `feat` | 기능 추가·변경 |
| `fix` | 버그 수정 |
| `refactor` | 동작은 그대로, 구조만 바꿈 |
| `perf` | 성능 개선 |
| `test` | 테스트만 추가·수정 |
| `docs` | 문서, ADR |
| `build` | Gradle, 의존성, Dockerfile |
| `ci` | GitHub Actions |
| `chore` | 그 밖의 잡일 |

- 범위: `auth`, `study`, `webhook`, `judging`, `notification`, `ledger`, `infra`, `frontend`
- 예: `feat(webhook): GitHub push 웹훅 서명 검증`
- 작업 브랜치 안의 커밋은 자유롭게 써도 돼요. 대신 남에게 보여 줄 수 있는 문장으로요.

## 4. PR

1. 작업을 시작하면 바로 **Draft PR**을 열어요. 진행 상황이 PR에 쌓여요.
2. 끝나면 템플릿을 채우고 **Ready for review**로 바꿔요. 해당 없는 칸은 지워요.
3. 본문에 `Closes #이슈번호`를 적어 병합하면 이슈가 닫히게 해요.

## 5. 리뷰

혼자라서 승인(Approve)은 할 수 없어요. 대신 아래를 지켜요.

**셀프 리뷰**
- GitHub의 Files changed 화면에서 diff를 처음부터 끝까지 읽어요. 에디터에서 보는 것과 다르게 보여요.
- 핵심 도메인 PR(판정, 스터디 일 배정, 웹훅 멱등 처리, 원장)은 **다음 세션 첫 10분에 다시 읽고** 병합해요.
- "왜 이렇게 했지?"가 나올 만한 줄에는 내 PR에 리뷰 코멘트로 이유를 남겨요. 큰 결정이면 ADR로 옮겨요.

**AI 리뷰 (선택)**
- AI에게 diff 리뷰를 맡길 수 있어요. 지적마다 수용·거절과 이유를 PR 코멘트로 남기고, `docs/01-AI Usage/`에도 기록해요.
- AI 리뷰는 셀프 리뷰를 대신하지 않아요.

**외부 리뷰**
- 핵심 PR 몇 개는 현업 개발자에게 리뷰를 부탁해요. 받은 코멘트와 반영 내용이 PR에 그대로 남아요.

## 6. 이슈

- 한 세션을 넘는 작업, 결정이 필요한 작업은 이슈를 먼저 만들어요. 오타 수정 같은 건 이슈 없이 PR만 열어도 돼요.
- 템플릿 세 가지: **기능**, **버그**, **실험** (측정해서 비교하는 일).
- 단계(P1~P4)는 **마일스톤**으로, 종류와 영역은 **라벨**로 나눠요.

| 라벨 | 뜻 |
|---|---|
| `type: feature` / `bug` / `experiment` / `chore` / `docs` | 종류 |
| `area: auth` / `study` / `webhook` / `judging` / `notification` / `ledger` / `infra` / `frontend` | 영역 |
| `adr` | 결정 기록이 필요함 |

## 7. 보안

- 비밀값(토큰, 키, 비밀번호)은 절대 커밋하지 않아요. 환경 변수와 GitHub Actions 시크릿으로만 다뤄요.
- 버그 이슈에 운영 로그를 붙일 땐 사용자 정보, 토큰, 웹훅 원문을 지워요. 공개 레포예요.
- 보안 취약점은 공개 이슈가 아니라 비공개 제보(Security advisories)로 받아요.

## 8. 레포 설정 순서

브랜치 규칙이 켜지면 `master`에 직접 push할 수 없어요. 그래서 순서가 중요해요.

1. **레포를 공개(Public)로 만들어요.** 공개 레포라야 브랜치 규칙, Actions 표준 러너, 비밀 스캐닝을 무료로 쓸 수 있어요.
2. **첫 커밋을 `master`에 push해요.** 이 파일들(`.github/`, `CONTRIBUTING.md`, `scripts/`)과 프로젝트 골격을 함께 넣어요. 그 전에 `.github/ISSUE_TEMPLATE/config.yml`의 `OWNER/REPO`를 실제 레포 이름으로 바꿔요.
3. **`sh scripts/setup-github.sh`를 실행해요.** 아래를 한 번에 적용해요.
    - 병합 방식: squash만 허용, PR 제목을 커밋 제목으로, 병합 뒤 브랜치 삭제
    - 라벨, 마일스톤(P1~P3)
    - `master` 보호: 삭제·강제 push 금지, 선형 이력, PR 필수(승인 0, 코멘트 해결 필수)
    - Actions 기본 토큰 읽기 전용, Actions의 PR 승인 금지
    - Dependabot alerts·security updates, 비공개 취약점 제보, CodeQL
4. **첫 CI가 돈 뒤 `sh scripts/setup-github.sh require-ci <잡 이름>`을 실행해요.** CI 통과가 병합 필수 조건이 돼요. 잡 이름은 Actions 탭에서 확인해요.
5. **CodeQL이 경고로 건너뛰어졌다면** Java 코드가 올라간 뒤 3번을 한 번 더 실행해요. 이미 적용된 항목은 건너뛰거나 덮어써요.

스크립트로 안 되는 것도 있어요.

- 서드파티 액션은 커밋 SHA로 고정하고, `pull_request_target`은 쓰지 않아요 (계획 9.2). 워크플로를 쓸 때 지키는 규칙이에요.
- 포크 PR의 워크플로 실행 승인은 공개 레포 기본값(처음 기여하는 사람은 승인 필요)을 그대로 둬요.
