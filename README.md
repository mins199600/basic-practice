<img width="700" alt="ERD" src="https://github.com/user-attachments/assets/5043a5d1-c56c-4df6-bd11-52d16da439ac" />

## ERD

### 테이블 구성

#### member

회원 정보를 관리하는 테이블입니다.

- `id`: 회원 고유 ID
- `email`: 로그인에 사용하는 이메일
- `nickname`: 게시글 작성자명으로 표시되는 닉네임
- `password`: 암호화된 비밀번호
- `role`: 사용자 권한 구분 값 (`USER`, `ADMIN`)
- `reg_date`: 회원가입일
- `update_date`: 회원 정보 수정일
- `address`, `detail_address`, `postcode`: 회원 주소 정보

#### board

게시글 정보를 관리하는 테이블입니다.

- `id`: 게시글 고유 ID
- `title`: 게시글 제목
- `content`: 게시글 내용
- `created_at`: 게시글 작성일
- `modified_at`: 게시글 수정일
- `view_count`: 조회수
- `category`: 게시글 카테고리
- `is_notice`: 공지사항 여부
- `file_path`: 첨부파일 경로
- `member_id`: 게시글 작성자 회원 ID

### 테이블 관계

- 회원 1명은 여러 개의 게시글을 작성할 수 있습니다.
- 게시글 1개는 반드시 1명의 회원에게 속합니다.
- `board.member_id`는 `member.id`를 참조합니다.

### 설계 의도

게시글 테이블에는 작성자명을 직접 저장하지 않고, 작성자 회원 ID인 `member_id`만 저장하도록 설계했습니다.

작성자명은 게시글 조회 시 `member` 테이블과 JOIN하여 `member.nickname` 값을 가져옵니다.

이를 통해 회원이 닉네임을 변경하더라도 기존 게시글의 작성자명이 최신 닉네임으로 자동 반영되도록 했습니다.

또한 회원 권한은 `role` 컬럼으로 관리하며, 일반 사용자는 `USER`, 관리자는 `ADMIN`으로 구분합니다.
