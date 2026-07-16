# 논리 데이터 모델링 (Logical Data Model)

물리 ERD(`docs/erd.jpg`)에서 데이터 타입·엔진·인덱스 등 구현 세부사항을 걷어내고, 엔터티·속성·관계(카디널리티) 중심으로 재정리한 논리 모델입니다.

---

## 1. 엔터티 정의

### 1-1. 회원 · 시스템

**회원 (MEMBER)**
- PK: 회원ID (id)
- 속성: 이메일, 닉네임, 비밀번호, 권한, 가입일시, 주소, 상세주소, 우편번호, 정보수정일시, 삭제여부

**팝업 (POPUP)** — 독립 엔터티, 관계 없음
- PK: 팝업ID (id)
- 속성: 제목, 내용, 시작일, 종료일, 사용여부, 등록일, 수정일

**문제(구) (QUESTION)** — 예전 시험 시스템의 문제 은행
- PK: 문제ID (id)
- FK: 주제ID (topic_id) → 시험주제
- 속성: 내용, 보기1~4, 정답번호, 난이도, 활성여부, 등록일

### 1-2. 게시판

**게시글 (BOARD)**
- PK: 게시글ID (id)
- FK: 작성자ID (member_id) → 회원
- 속성: 제목, 내용, 작성일시, 수정일시, 조회수, 카테고리, 공지여부, 첨부파일경로

**댓글 (COMMENT)** — 자기참조(대댓글)
- PK: 댓글ID (id)
- FK: 게시글ID (board_id) → 게시글, 작성자ID (member_id) → 회원, 부모댓글ID (parent_id) → 댓글(자기참조)
- 속성: 내용, 작성일시, 수정일시

### 1-3. 자격증 학습

**자격증 (CERTIFICATION)**
- PK: 자격증ID (id)
- FK: 회원ID (member_id) → 회원
- 속성: 자격증명, 시험예정일, 상태, 메모, 등록일, 삭제여부

**출석 (ATTENDANCE)**
- PK: 출석ID (id)
- FK: 회원ID (member_id) → 회원
- 속성: 출석일자, 등록일시
- 제약: (회원ID, 출석일자) 조합 유일 — 하루 1건만 출석 가능

### 1-4. 시험 메타 · 문제 분류

**과목 (SUBJECT)**
- PK: 과목ID (id)
- FK: 자격증ID (certification_id) → 자격증
- 속성: 과목명, 표시순서, 등록일, 삭제여부

**자격증문제 (CERTIFICATION_QUESTION)**
- PK: 문제ID (id)
- FK: 자격증ID (certification_id) → 자격증, 과목ID (subject_id) → 과목
- 속성: 문제내용, 보기1~4, 정답번호, 해설, 등록일, 삭제여부

**시험분류 (EXAM_CATEGORY)**
- PK: 분류ID (id)
- 속성: 코드, 분류명, 등록일

**시험주제 (EXAM_TOPIC)**
- PK: 주제ID (id)
- FK: 분류ID (category_id) → 시험분류
- 속성: 코드, 주제명, 등록일

### 1-5. 응시 기록 · 통계

**회원별문제통계 (MEMBER_QUESTION_STAT)**
- PK: 통계ID (id)
- FK: 회원ID (member_id) → 회원, 문제ID (question_id) → 자격증문제
- 속성: 출제가중치, 정답횟수, 오답횟수, 최종풀이일시
- 제약: (회원ID, 문제ID) 조합 유일 — upsert 기준 키

**응시기록 (EXAM_ATTEMPT)**
- PK: 응시ID (id)
- FK: 회원ID (member_id) → 회원, 분류ID (category_id) → 시험분류
- 속성: 시작일시, 제출일시, 총문항수, 정답수, 점수

**응시답안 (EXAM_ATTEMPT_ANSWER)**
- PK: 답안ID (id)
- FK: 응시ID (attempt_id) → 응시기록, 문제ID (question_id) → 문제(구), 주제ID (topic_id) → 시험주제
- 속성: 선택답번호, 정답여부, 답변일시

---

## 2. 관계 정의 (카디널리티)

| 부모 엔터티 | 자식 엔터티 | 관계 | FK 속성 | 설명 |
|---|---|---|---|---|
| 회원 | 게시글 | 1:N | member_id | 회원 1명이 여러 게시글 작성 |
| 회원 | 댓글 | 1:N | member_id | 회원 1명이 여러 댓글 작성 |
| 게시글 | 댓글 | 1:N | board_id | 게시글 1건에 댓글 여러 건 |
| 댓글 | 댓글 | 1:N (자기참조) | parent_id | 댓글의 대댓글 |
| 회원 | 자격증 | 1:N | member_id | 회원이 준비 중인 자격증 등록 |
| 회원 | 출석 | 1:N | member_id | 회원의 일자별 출석 기록 |
| 자격증 | 과목 | 1:N | certification_id | 자격증 1개에 과목 여러 개 |
| 자격증 | 자격증문제 | 1:N | certification_id | 자격증 1개에 문제 여러 개 |
| 과목 | 자격증문제 | 1:N | subject_id | 과목 1개에 문제 여러 개 |
| 회원 | 회원별문제통계 | 1:N | member_id | 회원별 출제 가중치 추적 |
| 자격증문제 | 회원별문제통계 | 1:N | question_id | 문제별 통계 누적 |
| 회원 | 응시기록 | 1:N | member_id | 회원의 시험 응시 이력 |
| 시험분류 | 시험주제 | 1:N | category_id | 분류 1개에 주제 여러 개 |
| 시험분류 | 응시기록 | 1:N | category_id | 분류 단위 응시 |
| 시험주제 | 문제(구) | 1:N | topic_id | 주제 1개에 문제 여러 개 |
| 응시기록 | 응시답안 | 1:N | attempt_id | 응시 1건에 답안 여러 건 |
| 문제(구) | 응시답안 | 1:N | question_id | 문제별 응시 답안 누적 |
| 시험주제 | 응시답안 | 1:N | topic_id | 주제별 응시 답안 집계용 |

관계는 전부 1:N이며, N:M(다대다) 관계는 없습니다. 팝업(POPUP)은 다른 엔터티와 관계가 없는 독립 엔터티입니다.

---

## 3. 설계 특징

**소프트 삭제**: `member`, `certification`, `subject`, `certification_question`은 물리 삭제 대신 `deleted` 플래그로 상태만 변경합니다. 자격증학습 도메인 전반에 일관되게 적용된 패턴입니다.

**자기참조 구조**: `comment.parent_id`는 같은 테이블을 참조하여 댓글/대댓글을 하나의 엔터티로 표현합니다. 별도 테이블을 두지 않고 트리 구조를 재귀적으로 표현하는 전형적인 방식입니다.

**가중치 기반 통계**: `member_question_stat`은 (회원, 문제) 조합을 유니크 키로 두어 upsert(`INSERT ... ON DUPLICATE KEY UPDATE`)로 갱신되는 구조입니다. 오답노트/복습 우선순위 로직의 데이터 기반입니다.

**레거시 병행 구조**: `question` / `exam_topic` / `exam_category` / `exam_attempt` / `exam_attempt_answer`는 `certification_question` 계열과 별개로 존재하는 이전 시험 시스템입니다. 도메인 성격은 유사하지만 자격증(certification) 단위가 아닌 분류(exam_category)/주제(exam_topic) 단위로 설계되어 있어 논리적으로 분리된 서브모델로 취급했습니다.

**정규화 수준**: 대부분의 엔터티가 제3정규형(3NF)을 만족합니다. 예외적으로 `certification_question`, `question`의 `choice1~4`는 정규화하면 별도 보기(choice) 테이블로 분리 가능하지만, 보기 개수가 4개로 고정되어 있어 비정규화된 형태를 의도적으로 유지했습니다 (조회 성능·구현 단순성 우선).
