---
name: pr
description: 현상·원인·조치를 명시적으로 기술한 풀리퀘스트를 작성할 때 사용. 버그 수정, 기능 추가, 리팩토링 등 모든 PR 유형에 적용.
argument-hint: "[PR 제목 또는 추가 컨텍스트 (선택)]"
allowed-tools: Read, Grep, Glob, Bash
---

# PR 작성 지시

$ARGUMENTS

---

## 작업 목표

리뷰어가 코드를 읽기 전에 변경의 맥락과 이유를 완전히 이해할 수 있는 PR을 작성한다.
**목적 → 요약 → 상세**의 순서로 구성해, 리뷰어가 원하는 깊이에서 멈출 수 있어야 한다.

---

## 작업 절차

### Step 1. 변경 정보 수집

```bash
# 현재 브랜치와 커밋 스타일 파악
git branch --show-current
git log --oneline -5

# 변경 파일 구조 파악 (먼저 전체 구조를 본다)
git diff --stat origin/<BASE>..HEAD
git diff --name-status origin/<BASE>..HEAD

# 커밋 메시지
git log --format="%h %s%n%b" origin/<BASE>..HEAD

# 핵심 파일만 집중해서 읽기 (전체 diff는 큰 PR에서 비효율적)
# --stat 결과에서 변경량이 많거나 핵심적인 파일만 선별해서 읽는다
git diff origin/<BASE>..HEAD -- <핵심파일>
```

**베이스 브랜치 결정 방법**: `master → main → develop` 순으로 remote에 존재하는 브랜치를 확인한다. 현재 레포의 기본 브랜치가 명확하면 그것을 사용한다. 확실하지 않으면 `[확인 필요: base branch]`로 표시한다.

### Step 2. 현상 도출

**현상** = 이 PR이 없을 때 사용자/시스템이 겪는 문제 또는 부재하는 기능.

- 버그 수정: 어떤 조건에서 어떤 오류/오동작이 발생했는가
- 기능 추가: 무엇이 불가능했는가
- 리팩토링: 어떤 품질 문제가 있었는가

> "MFA 완료 후 서비스동의 케이스에서 LoginLevel이 TWO_FACTOR_LOGIN 대신 LOGIN으로 기록됨"
> 형태로 — **구체적인 조건 + 구체적인 증상**을 함께 기술한다.

### Step 3. 원인 분석

**원인** = 현상이 발생한 코드상의 근본 원인.

- 어느 파일·함수·라인에서 문제가 시작되었는가
- 왜 그 코드가 그렇게 동작했는가 (설계 누락, 잘못된 가정, 경쟁 조건 등)
- 단순 "A가 잘못됨"이 아니라 **인과 관계**를 명시한다

### Step 4. 조치 기술

**조치** = 원인을 제거하기 위해 실제로 한 것.

- 어느 파일을 어떻게 바꿨는가, 왜 그 방식을 선택했는가
- **Before / After 다이어그램**: 호출 흐름 변화를 ASCII로 시각화 (단순 1줄 수정이면 생략)
- 각 파일의 **리뷰 포인트**: 리뷰어가 특히 집중해야 할 판단 포인트

### Step 5. PR 본문 작성

아래 형식으로 출력한다.

---

## PR 출력 형식

아래 섹션 순서대로 PR 본문을 작성한다.

### `## 목적`

이 PR을 만든 이유와 달성하려는 목표를 **1~2문장**으로 작성한다.
리뷰어가 "왜 이 PR이 필요한가"를 가장 먼저 이해할 수 있어야 한다.

예:
> MFA 완료 후 서비스동의가 필요한 로그인 흐름에서 MFA 완료 상태가 유실되어 로그인 이력이 단일인증으로 기록되는 문제를 수정한다.

### `## 요약`

리뷰어가 코드를 읽기 전에 핵심을 파악할 수 있도록 bullet로 작성한다.

```
- 문제: (현상 한 줄)
- 원인: (코드 수준 원인 한 줄)
- 변경: (무엇을 어떻게 바꿨는지 한 줄)
- 테스트: (어떤 테스트로 검증했는지 한 줄)
- 리뷰 포인트: (가장 중점적으로 봐야 할 부분 한 줄)
```

### `## 현상`

어떤 조건에서 무슨 문제가 발생했는지 (사용자/시스템 관점).

### `## 원인`

근본 원인 — 파일/함수 수준으로 구체적으로.

### `## 조치`

무엇을 어떻게 바꿨는지. 파일별 변경 테이블 포함.

| 파일       | 변경 내용  | 리뷰 포인트     |
|----------|--------|------------|
| `파일명.kt` | 의도 한 줄 | 판단이 필요한 부분 |

**Before / After 다이어그램** — 변경 전후의 호출 흐름을 코드블록으로 표현한다.
복잡한 변경일수록 효과적이다. 단순 1줄 수정이면 생략.

예시:

```
[Before]
forceLogout()
  → createAuthToken(Sub, memberNo)        # 서브 테넌트 키 조회 ✅
  → createNeoForcedLogoutParam(idNo, ...) # 루트 키 하드코딩 ❌
  → forcedLogout(...)                      # 실패

[After]
forceLogout()
  → createAuthToken(Sub, memberNo)                                      # 서브 테넌트 키 조회 ✅
  → createNeoForcedLogoutParam(idNo, ..., authToken.tenant.consumerKey) # 서브 키 전달 ✅
  → forcedLogout(...)                                                    # 성공
```

### `## 리뷰 포인트`

리뷰어가 특히 집중해서 봐야 할 파일·로직·판단 기준을 열거한다.
"이 부분이 맞는지 확인해달라"는 요청을 명시적으로 담는다.

예:
> - `ServiceAgreementService.confirmServiceAgreement`: MFA 여부 복원 로직이 올바른지
> - Redis value 변경이 기존 서비스동의 흐름과 호환되는지
> - LoginLevel 결정 기준이 다른 로그인 흐름과 일관적인지

### `## 변경 범위`

- **영향 있는 흐름**: 이 변경이 동작에 영향을 주는 시나리오
- **영향 없는 흐름**: 코드 경로는 지나지만 기존 동작이 유지되는 시나리오

> 리뷰어의 "다른 데 영향 없어?" 질문을 선제 차단하는 섹션. 반드시 작성한다.

### `## 테스트`

자동 테스트와 수동 검증을 구분해서 표로 작성한다.

| 구분     | 항목                   | 결과 |
|--------|----------------------|----|
| 자동 테스트 | `./gradlew :모듈:test` | 통과 |
| 수동 검증  | 검증 시나리오 설명           | 확인 |

### `## 테스트 계획` *(선택 — 아래 조건 중 하나 이상 해당 시 작성)*

다음 경우에만 작성한다. 해당 없으면 섹션 자체를 생략한다.

- 배포 순서나 단계가 있는 경우 (모듈 간 의존성, 마이그레이션 등)
- 리뷰어 또는 QA가 staging/dev에서 직접 시나리오를 실행해야 하는 경우
- 배치·스크립트 등 수동 실행이 필요한 경우
- 롤백 조건이나 모니터링 포인트가 있는 경우

```
**배포 순서** (해당 시)
1. `모듈A` 먼저 배포 — 이유: ...
2. `모듈B` 배포 — 이유: ...

**검증 시나리오**
| 단계 | 시나리오 | 기대 결과 | 확인 방법 |
|------|---------|----------|----------|
| 1 | ... | ... | 로그/API/화면 |

**롤백 조건** (해당 시)
- 조건: ... → 롤백 방법: ...

**모니터링 포인트** (해당 시)
- 배포 후 N분간 확인할 로그/지표: ...
```

### `## 참고`

관련 이슈, 슬랙 스레드, 문서 링크. 없으면 섹션 자체를 생략한다.

---

## 작성 원칙

### 목적은 "왜"로 시작한다

- ❌ "LoginLevel 필드를 수정함"
- ✅ "MFA 완료 상태가 유실되어 로그인 이력이 잘못 기록되는 문제를 수정한다"

### 현상은 사용자/시스템 관점으로

- ❌ "LoginLevel이 하드코딩되어 있었음" → 원인 서술
- ✅ "MFA 완료 사용자의 로그인 이력이 단일인증으로 잘못 기록됨" → 현상 서술

### 원인은 인과 관계로

- ❌ "`processAfterAgreementConfirmed`가 LoginLevel.LOGIN을 사용함"
- ✅ "`markServiceAgreementPending` 호출 시 MFA 완료 여부가 저장되지 않아, `confirmServiceAgreement` 시점에 MFA 여부를 판별할 수 없었음"

### 조치는 Why를 포함해서

- ❌ "`markServiceAgreementPending`에 파라미터 추가"
- ✅ "Redis value에 MFA 여부를 함께 저장해, 서비스동의 확정 시점에 LoginLevel을 동적으로 결정하도록 수정. Member 모델 변경 없이 최소 영향으로 처리."

### 리뷰 포인트는 판단이 필요한 부분에만

모든 변경을 나열하는 게 아니라, **의도가 맞는지 확인이 필요한 부분**만 적는다.

### 없는 정보는 추론하지 말고 `[확인 필요]` 표시

이슈 번호, 슬랙 링크 등 컨텍스트가 없으면 빈칸으로 두거나 `[확인 필요]`로 명시한다.


---

## `gh pr create` / `gh pr edit` 호출 시 주의 — 백틱(\`) escape 금지

`gh pr edit 3896 --body "$(cat <<'EOF' ... EOF)"` 형태로 본문을 전달할 때, **single-quoted heredoc (`<<'EOF'`)** 안에서는 shell 이
변수·명령·백틱 치환을 하지 **않는다**. 따라서 본문 안의 백틱(`` ` ``)을 `\` ` 로 escape 할 필요가 없다.

- ❌ 잘못된 예 (PR 본문에 `\` 가 그대로 들어감 → 코드 블록이 깨짐):
  ```
  $(cat <<'EOF'
  ## 조치
  \`RefreshTokenService\` 의 \`updateById\` → ...
  \`\`\`sql
  UPDATE ...
  \`\`\`
  EOF
  )
  ```
- ✅ 올바른 예 (백틱 그대로):
  ```
  $(cat <<'EOF'
  ## 조치
  `RefreshTokenService` 의 `updateById` → ...
  ```sql
  UPDATE ...
  ```
  EOF
  )
  ```

만약 이미 잘못 escape 한 PR 본문이 올라갔다면, 일괄 fix 가능:

```bash
gh pr view <PR번호> --json body --jq .body \
  | sed 's/\\`/`/g' \
  | gh pr edit <PR번호> --body-file -
```

**⚠️ pipeline 으로 PR 본문 update 시 절대 주의**

`gh ... | sed ... | gh pr edit --body-file -` 형태는 **앞 명령이 실패해 에러 메시지가 stdout 으로 흘러가면 그 에러 메시지가 PR 본문이 됨** (실제 사고: 4 PR
본문이 모두 `{"message":"Not Found",...}` 로 덮어써짐).

안전한 패턴:

```bash
# (a) 중간 파일에 받고 검증 후 update
gh pr view <PR번호> --json body --jq .body > /tmp/pr-body.md
[[ -s /tmp/pr-body.md ]] || { echo "FAIL: 빈 본문"; exit 1; }
grep -q "Not Found" /tmp/pr-body.md && { echo "FAIL: 에러 응답"; exit 1; }
sed -i '' 's/\\`/`/g' /tmp/pr-body.md
gh pr edit <PR번호> --body-file /tmp/pr-body.md

# (b) 또는 pipefail + set -e 로 명령 실패 시 즉시 중단
set -euo pipefail
gh pr view <PR번호> --json body --jq .body | sed 's/\\`/`/g' | gh pr edit <PR번호> --body-file -
```

`gh api` 의 404 출력은 stderr 가 아닌 stdout 으로 나올 수 있으니 더더욱 위험. **다중 PR 을 for loop 으로 돌릴 때는 반드시 단일 PR 로 dry-run 부터 검증할 것.**

**기억할 것**:

- `<<'EOF'` (single quote) → escape 불필요 (권장)
- `<<EOF` (no quote) → 변수·명령·백틱 모두 evaluate 됨 → escape 필요하지만 의도치 않은 치환 위험. PR 본문 작성에는 부적합.
