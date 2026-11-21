# 코테 스터디 자동 운영 서비스

> 풀기만 하세요. 인증·알림·정산은 봇이 합니다.

코딩테스트 스터디를 자동으로 운영하는 서비스예요. GitHub에 풀이가 올라오면 자동으로 인증하고, 마감 전에 알림을 보내고, 판정과 정산을 대신해요.

## 문서

- [실행 계획](docs/Codingtest%20study%20service%20plan.md): 문제 정의, 범위, 설계, 로드맵
- [ADR](docs/00-Architectural%20Decision%20Records/): 결정 기록
- [AI 활용 기록](docs/01-AI%20Usage/)
- [작업 방식](CONTRIBUTING.md)

## 로컬 실행

Java 21과 Docker가 필요해요. GitHub 로그인을 해 보려면 개발용 GitHub App의 `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`을 환경변수로 넣어 주세요 (`.env`, 커밋 금지). 테스트는 없어도 돌아가요.

```sh
./gradlew build         # 테스트 (Testcontainers로 PostgreSQL을 띄워요)
./gradlew bootTestRun   # 로컬 실행 (PostgreSQL 컨테이너와 함께)
```
