# 프로젝트 개요 (login-crud)

> 이 문서는 프로젝트 전체 구조를 한눈에 파악하기 위한 요약본입니다. 상세 스키마는 [README.md](../README.md), 배포 절차는 [DEPLOYMENT_PLAN.md](../DEPLOYMENT_PLAN.md)를 참고하세요.

## 1. 기술 스택
- **빌드**: Gradle
- **언어/프레임워크**: Java 17, Spring Boot 3.5.13
- **DB 연동**: MyBatis 3.0.5 + MariaDB (`mariadb-java-client:3.3.3`)
- **뷰**: Thymeleaf
- **기타**: Spring Security Crypto(비밀번호 암호화 용도, Security 프레임워크 자체는 미사용), Spring Mail, Lombok
- **AI 연동**: Anthropic Claude API (RestClient, `AiIntegrationConfig`) — 프로젝트 면접 첨삭, 코딩테스트 채점/첨삭에 사용
- **배포**: AWS EC2, systemd(`deploy/login-crud.service`), Nginx(`deploy/nginx.conf`), GitHub Actions(`.github/workflows/deploy.yml`)

## 2. 도메인 패키지 구조 (`src/main/java/com/practice/logincrud`)

| 패키지 | 역할 | 주요 클래스 |
|---|---|---|
| `member` | 회원가입/로그인/이메일 인증 | `MemberController`, `MemberService`, `MemberMapper`, `EmailAuthService` |
| `admin` | 관리자 페이지 (회원/게시글/팝업 관리, 대시보드) | `AdminController/Service`, 하위 `adminconfig`(인터셉터), `dashboard` |
| `board` | 게시판(오답노트) CRUD, 페이징 | `BoardController/Service/Mapper`, `PageDto` |
| `comment` | 댓글/대댓글 (자기참조 `parent_id`) | `CommentController/Service/Mapper` |
| `certification` | 자격증 등록·퀴즈·과목 분류·오답 통계 | `CertificationController/Service`, `CertificationQuestionController/Service`(퀴즈), `SubjectService`, `MemberQuestionStatMapper`, `WeakestSubjectDto` |
| `attendance` | 출석체크 | `AttendanceController/Service/Mapper` |
| `interview` | 프로젝트 면접 준비 (GitHub 연동 + Claude AI 질문/첨삭) | `GitHubService`, `ClaudeAiService`, `InterviewSession/Message/Project` |
| `codingtest` | AI 코딩테스트 (문제 생성·답안 첨삭, 코드 실행 없이 텍스트 기반) | `CodingTestController/Service`, `CodingTestAiService`, `CodingTestSessionDto/Mapper`, `CodingTestMessageDto/Mapper` |
| `stats` | 홈 대시보드 통계 | `StatsService`, `SkillStatsDto`, `StudySummaryDto/Service`(자격증+코딩테스트 활동 합산) |
| `config` | 공통 설정 | `WebConfig`, `ApiConfig`, `AiIntegrationConfig`, `SessionConst` |
| `interceptor` | 로그인 인터셉터 | `LoginInterceptor` |

## 3. MyBatis 매퍼 (`src/main/resources/mapper`)
`login.xml`(회원), `board.xml`, `comment.xml`, `admin.xml`, `adminDashboard.xml`, `attendance.xml`, `certification.xml`, `certificationQuestion.xml`, `subject.xml`, `memberQuestionStat.xml`, `interviewSession/Message/Project.xml`, `codingTestSession.xml`, `codingTestMessage.xml`

## 4. 화면 (`src/main/resources/templates`)
- `home.html`(대시보드), `main.html`, `signup.html`, `edit.html`
- `member/`: 아이디/비밀번호 찾기
- `board/`: 목록/상세/작성/수정
- `certification/`: 자격증 등록/수정/목록, 퀴즈, 과목별 목록
- `attendance/calendar.html`
- `interview/`: 목록/상세/채팅
- `codingtest/`: 목록/상세
- `admin/`: 로그인, 대시보드, 회원/게시글/팝업 관리, 비밀번호 찾기

## 5. DB 마이그레이션 이력 (`sql/`)
| 파일 | 내용 |
|---|---|
| 002 | certification, attendance 테이블 생성 |
| 003 | certification_question(문제은행) + member_question_stat(오답 가중치) |
| 004 | 피부미용 자격증 문제 시드 데이터 |
| 005 | subject(과목) 테이블 추가 |
| 006~008 | 로컬/운영 간 certification_id 불일치 보정, 과목 재분류 핫픽스 |
| 009 | interview_project 등 프로젝트 면접 준비 테이블 |
| 010 | member.profile_image 컬럼 추가 |
| 011 | coding_test_session, coding_test_message 등 AI 코딩테스트 테이블 |

ERD/논리 모델은 [docs/logical_data_model.md](logical_data_model.md), [docs/erd.jpg](erd.jpg) 참고.

## 6. 현재 진행 중인 작업 (워킹트리 기준)
최근 커밋 흐름(`3ec2ea3` 프로젝트 면접 준비 기능 → `acd8a65` 프로필 사진/실력 분석/문제풀이 방식 개편)에 이어, 다음 두 가지를 동시에 진행 중:

1. **AI 코딩테스트 기능 신규 추가**: `codingtest` 패키지 전체, `sql/011_coding_test.sql`, `codingTestSession.xml`/`codingTestMessage.xml`, `templates/codingtest/`, `codingtest.css`, `codingtest-detail.js` — Claude API로 문제 생성 및 답안 첨삭
2. **홈 대시보드 실력 분석 고도화**: `StatsService`/`SkillStatsDto` 실계산 반영, `WeakestSubjectDto`(취약 과목), `StudySummaryDto/Service`(자격증+코딩테스트 학습량 합산), `memberQuestionStat.xml` 통계 쿼리 추가, `home.html`/`home.css`에 학습 요약 카드 반영

## 7. 관련 문서
- [README.md](../README.md) — DB 스키마 상세, ERD
- [DEPLOYMENT_PLAN.md](../DEPLOYMENT_PLAN.md) — AWS EC2 배포 계획
- [TROUBLESHOOTING_2026-07-15.md](../TROUBLESHOOTING_2026-07-15.md), [WORK_LOG_2026-07-14.md](../WORK_LOG_2026-07-14.md) — 작업 이력
- [docs/logical_data_model.md](logical_data_model.md) — 논리 데이터 모델
