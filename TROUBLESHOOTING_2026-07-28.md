# 트러블슈팅 로그 — 2026-07-28
## 자격증 문제풀이 매칭 실패 — 스키마 설계 결함 진단 및 개선 구조 설계

### 배경

회원가입 후 로그인한 신규 회원이 "자격증 문제풀이"에서 "피부(스킨케어) 자격증"을 등록해도 "아직 등록된 문제가 없습니다" 화면만 뜨는 문제가 보고됨. 그런데 다른(먼저 가입한) 회원 계정에서는 동일한 이름의 자격증으로 문제풀이가 정상 동작함 — 같은 기능인데 계정에 따라 되고 안 되는 현상.

### 목표

증상의 원인을 스키마 레벨에서 진단하고, AS-IS/TO-BE ERD로 문제 구조와 개선 구조를 문서화한다. (이 문서는 설계 단계 기록이며, 실제 마이그레이션/코드 구현은 별도 작업으로 진행한다.)

---

### 원인 — `certification_id`가 "회원 개인 기록 id"와 "문제은행 식별자"를 겸하고 있었음

`certification` 테이블은 회원이 "내 자격증"을 추가할 때마다 새로운 행이 생기는 **개인별 트래킹 레코드**다. `id`는 회원마다 제각각 다른 값으로 auto_increment 발급된다.

그런데 실제 시험 문제은행(`certification_question`, `subject`)은 이 개인별 `certification.id` 중 **딱 하나의 특정 값**에만 고정 연결되어 있었다. 그 값을 우연히 발급받은 회원(대부분 가장 먼저 만든 계정)만 문제가 보이고, 나머지 회원은 매칭되는 문제가 하나도 없어서 "아직 등록된 문제가 없습니다" 화면으로 빠진다.

과거 마이그레이션 이력(`sql/006_fix_local_subject_mismatch.sql`, `sql/007_classify_beauty_subject.sql`, `sql/008_fix_beauty_certification_id.sql`)을 보면, 이 문제가 "로컬 DB는 피부자격증 id가 3번, EC2는 1번이라 서로 안 맞음" 형태로 이미 여러 번 터졌었고, 그때마다 숫자를 하드코딩으로 패치해온 것이 확인된다. 근본 원인은 그대로 둔 채 증상만 계속 고쳐온 상태였다.

#### AS-IS: 회원마다 다른 id가 발급되고, 문제은행은 그중 하나에만 고정

```mermaid
erDiagram
    MEMBER ||--o{ CERTIFICATION : "회원이 등록 (매번 새 id 발급)"
    CERTIFICATION ||--o{ SUBJECT : "certification_id로 연결"
    CERTIFICATION ||--o{ CERTIFICATION_QUESTION : "certification_id로 연결"

    MEMBER {
        bigint id PK
    }
    CERTIFICATION {
        bigint id PK "회원마다 다른 값!"
        bigint member_id FK
        varchar cert_name "자유 텍스트, 이름 같아도 id는 다름"
    }
    SUBJECT {
        bigint id PK
        bigint certification_id FK "특정 값(예:3)에만 고정"
        varchar name
    }
    CERTIFICATION_QUESTION {
        bigint id PK
        bigint certification_id FK "특정 값(예:3)에만 고정"
        bigint subject_id FK
    }
```

회원 A가 "피부(스킨케어) 자격증"을 등록해 `certification.id = 3`을 받으면 문제가 정상적으로 보이지만, 회원 B가 같은 이름으로 등록해 `id = 57`을 받으면 `subject`/`certification_question`이 바라보는 `certification_id = 3`과 안 맞아서 문제가 하나도 안 보인다.

---

### 개선 구조 — `certification_catalog`(자격증 종류 마스터) 도입

"자격증 종류"라는 개념을 별도의 표준 테이블로 분리하고, 문제은행(`subject`, `certification_question`)은 회원 개인 기록이 아니라 **이 표준 테이블의 id만 바라보도록** 구조를 바꾼다. 회원 개인의 `certification` 기록은 이 표준 테이블을 참조는 하되, 강하게 묶지는 않는다(아래 "FK 설계 조정" 참고 — 12개 역할 리뷰에서 나온 결론 반영).

#### TO-BE: 문제은행이 회원과 무관한 표준 id 하나만 바라봄

```mermaid
erDiagram
    MEMBER ||--o{ CERTIFICATION : "회원이 등록 (매번 새 id 발급, 그대로 유지)"
    CERTIFICATION_CATALOG ||--o{ CERTIFICATION : "catalog_id (느슨한 연결 - 인덱스만, 강한 FK 아님)"
    CERTIFICATION_CATALOG ||--o{ SUBJECT : "catalog_id FK (필수)"
    CERTIFICATION_CATALOG ||--o{ CERTIFICATION_QUESTION : "catalog_id FK (필수)"

    MEMBER {
        bigint id PK
    }
    CERTIFICATION_CATALOG {
        bigint id PK "자격증 종류당 딱 하나"
        varchar name UK "이름 유일 제약"
    }
    CERTIFICATION {
        bigint id PK "회원마다 다른 값 (그대로)"
        bigint member_id FK
        varchar cert_name "자유 텍스트 입력은 그대로 유지"
        bigint catalog_id FK "등록 시 이름 매칭으로 자동 연결"
    }
    SUBJECT {
        bigint id PK
        bigint catalog_id FK "회원과 무관, 표준 id 고정"
        varchar name
    }
    CERTIFICATION_QUESTION {
        bigint id PK
        bigint catalog_id FK "회원과 무관, 표준 id 고정"
        bigint subject_id FK
    }
```

회원이 자격증을 추가하면(`CertificationService.save()`), 입력한 이름으로 `certification_catalog`를 찾거나 없으면 새로 만들어(`getOrCreateByName`) `catalog_id`를 자동으로 연결한다. 사용자 입력 화면(자유 텍스트 입력)은 그대로 두고, 매칭은 서버 내부에서 처리한다.

#### FK 설계 조정 — "FK 남발" 우려 반영

처음 설계안은 `certification`, `subject`, `certification_question` 세 곳 모두에 `catalog_id` FK를 강하게 거는 안이었으나, 12개 역할(아키텍처/DB 전문가 포함) 리뷰에서 과도한 정규화라는 지적이 나와 아래처럼 조정했다.

| 테이블 | 연결 방식 | 이유 |
|---|---|---|
| `subject.catalog_id` | **강한 FK** | 문제은행이 회원과 무관하게 항상 일관된 카탈로그를 가리켜야 이번 버그가 재발하지 않음 — 참조 무결성이 실제로 버그를 막아주는 지점 |
| `certification_question.catalog_id` | **강한 FK** | 위와 동일 |
| `certification.catalog_id` | **인덱스만(느슨한 연결), `ON DELETE SET NULL`** | 회원 개인 기록은 카탈로그가 나중에 정리/병합돼도 깨지면 안 되는 자유도가 필요 — 강한 제약은 유연성만 해침 |

자세한 리뷰 근거는 [docs/reviews/2026-07-28-certification-catalog-plan-review.md](docs/reviews/2026-07-28-certification-catalog-plan-review.md) 참고.

---

### 검증 (설계 단계 검증)

- 12개 전문가 역할 스킬(`.claude/skills/review-*`)로 계획을 각각 검토 → 종합 판정 **조건부 승인**
- 지적된 3가지 보완사항(FK 축소, `catalog_id`가 null인 개인 기록의 화면 처리, 이름 매칭 실패 케이스 테스트)을 이 문서의 FK 설계 조정에 반영함
- 실제 마이그레이션 적용 후 검증은 별도로 진행 예정 — 배포 전 아래를 반드시 확인:
  ```sql
  -- 백필 전 이름 편차 확인
  SELECT DISTINCT TRIM(cert_name) FROM certification WHERE deleted = 0;
  -- 백필 후 고아 데이터(카탈로그 매칭 실패) 확인
  SELECT COUNT(*) FROM certification WHERE catalog_id IS NULL AND deleted = 0;
  ```

### 재발 방지 체크리스트

- [ ] 여러 회원이 공유해야 하는 데이터(문제은행 등)를 "회원 개인 레코드의 id"에 직접 의존시키지 않는다 — 공유 개념은 항상 별도 마스터 테이블로 분리
- [ ] FK를 추가할 때 "이 제약이 실제로 막아주는 이상 데이터가 무엇인지"를 먼저 확인하고, 막연히 정규화 원칙만으로 FK를 걸지 않는다
- [ ] 이름(문자열) 기준으로 데이터를 매칭할 때는 트림/대소문자 편차를 반드시 사전 확인한다
- [ ] 스키마 설계는 코딩 착수 전에 다중 관점(백엔드/DB/아키텍처 등) 리뷰를 거친다

### 면접 어필 포인트

- 서로 다른 계정에서 같은 기능이 되고 안 되는 비일관적 버그를, 재현 조건(계정별 차이)을 단서로 스키마 레벨까지 추적해 근본 원인을 특정함
- "임시 패치를 반복해온 이력(과거 마이그레이션 3건)"을 근거로, 증상 치료가 아닌 구조적 원인 해결(마스터 테이블 분리)을 선택한 의사결정 과정을 설명할 수 있음
- FK를 무조건 추가하지 않고, 각 관계마다 "강한 제약이 필요한 지점 vs 유연성이 더 중요한 지점"을 구분해 설계한 트레이드오프 판단 경험
