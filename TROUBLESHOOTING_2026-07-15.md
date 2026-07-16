# 트러블슈팅 로그 — 2026-07-15
## EC2 운영 DB에 자격증 문제 데이터(79건) 시딩

### 배경
로컬 DB에는 "피부자격증"(certification_id=1) 문제 79개(`certification_question`)를 미리 넣어뒀는데, 배포된 EC2 사이트에서는 문제가 안 보이는 문제 발생. 원인은 로컬 DB와 EC2 DB가 완전히 분리된 별도 인스턴스라 **스키마만 동기화됐고 데이터는 동기화된 적이 없었기 때문**.

### 목표
로컬에만 있던 INSERT 문 79건을 `sql/004_seed_skincare_questions.sql`로 정리 → EC2 운영 DB(`easy`)에 반영.

---

### 에러 1 — scp 파일 경로 오류

```
C:\Users\mins1>scp -i "C:\Users\mins1\my-key.pem" sql\004_seed_skincare_questions.sql ec2-user@13.124.5.156:/tmp/
scp: stat local "sql/004_seed_skincare_questions.sql": No such file or directory
```

**원인**: PC 터미널이 프로젝트 폴더가 아니라 `C:\Users\mins1>`에서 실행됨. 상대경로(`sql\...`)라서 실제 파일을 못 찾음.

**해결**:
```
cd C:\Users\mins1\OneDrive\Desktop\login-crud
scp -i "C:\Users\mins1\my-key.pem" sql\004_seed_skincare_questions.sql ec2-user@13.124.5.156:/tmp/
```

**교훈**: scp/git 등 상대경로를 쓰는 명령어는 실행 전 항상 `cd`로 프로젝트 루트 확인부터.

---

### 에러 2 — mariadb 프롬프트 안에서 bash 리다이렉션 명령 실행 → ERROR 1064

EC2 접속 후 SQL을 넣으려고 `sudo mariadb easy` 로 mariadb 콘솔에 먼저 들어간 상태에서, bash 전용 명령인 아래 줄을 그대로 입력함:

```
MariaDB [easy]> sudo mariadb easy < /tmp/004_seed_skincare_questions.sql
```

**원인**: `< /tmp/...` 리다이렉션은 bash 셸 문법이지 SQL 문법이 아님. mariadb 프롬프트는 이걸 SQL로 해석하려다 실패:
```
ERROR 1064 (42000): You have an error in your SQL syntax...
```

**해결 과정**:
1. `Ctrl+C` / `Ctrl+Z`로 빠져나오려다 프로세스가 죽지 않고 백그라운드에 stopped 상태로 남음 → `jobs`에 `[2]+ Stopped sudo mariadb easy` 표시.
2. `kill %2`로 정지된 job 정리.
3. **bash 프롬프트(`[ec2-user@... ~]$`)인지 mariadb 프롬프트(`MariaDB [easy]>`)인지 먼저 확인**한 뒤, bash 프롬프트에서 다시 실행:
   ```
   sudo mariadb easy < /tmp/004_seed_skincare_questions.sql
   ```

**교훈**: SQL 파일을 콘솔에 붙여넣거나 리다이렉션할 땐 반드시 bash 프롬프트인지 확인. mariadb 안에 있을 땐 `exit` 또는 `quit`으로 먼저 나올 것.

---

### 검증

```sql
sudo mariadb easy -e "SELECT COUNT(*) FROM certification_question WHERE certification_id=1;"
```
결과: `79` — 정상 반영 확인.

```
ls -la /tmp/004_seed_skincare_questions.sql
-rw-rw-r--. 1 ec2-user ec2-user 33667 Jul 15 16:43 /tmp/004_seed_skincare_questions.sql
```
파일 전송도 정상.

---

### 재발 방지 체크리스트
- [ ] 로컬 DB에 새 데이터/스키마 추가 시, EC2 반영 여부를 그때그때 같이 기록 (스키마와 데이터는 별개로 동기화해야 함을 항상 기억)
- [ ] EC2 터미널 작업 전, 프롬프트 모양으로 현재 위치(bash vs mariadb vs 로컬 PC) 먼저 확인
- [ ] SQL 파일 반영은 되도록 `mysql/mariadb -e "..."` 한 줄 명령이나 리다이렉션(`<`)으로 통일하고, 대화형 콘솔에 직접 붙여넣기는 지양
- [ ] 데이터 시딩 후 반드시 `COUNT(*)` 등으로 결과 검증 — job 메시지(Stopped 등)만으로는 성공 여부를 신뢰하지 말 것

### 면접 어필 포인트
- 운영/로컬 DB가 물리적으로 분리된 환경에서 스키마와 데이터 동기화 문제를 직접 진단하고, 검증 쿼리로 결과를 확인하는 절차를 세움
- bash와 DB 클라이언트 셸(mariadb REPL)의 문맥 차이를 이해하고 트러블슈팅함 — CLI 환경에 대한 기본기
- 배포 후 육안 확인에 의존하지 않고 `SELECT COUNT(*)` 등 재현 가능한 검증 스텝을 습관화
