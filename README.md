# ERD

<img width="747" height="501" alt="ERD" src="https://github.com/user-attachments/assets/24c544aa-8274-4865-8d85-979cf8df2302" />

## 테이블 구성

### member

회원 정보를 관리하는 테이블입니다.

- `id`: 회원 고유 ID
- `email`: 로그인에 사용하는 이메일
- `nickname`: 게시글 작성자명으로 표시되는 닉네임
- `password`: 암호화된 비밀번호
- `role`: 사용자 권한 구분 값 (`USER`, `ADMIN`)
- `reg_date`: 회원가입일
- `update_date`: 회원 정보 수정일
- `address`, `detail_address`, `postcode`: 회원 주소 정보
- `deleted`: 회원 삭제 여부를 저장하는 값

### board

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

---

## 테이블 관계

- 회원 1명은 여러 개의 게시글을 작성할 수 있습니다.
- 게시글 1개는 반드시 1명의 회원에게 속합니다.
- `board.member_id`는 `member.id`를 참조합니다.

---

## 설계 의도

게시글 테이블에는 작성자명을 직접 저장하지 않고, 작성자 회원 ID인 `member_id`만 저장하도록 설계했습니다.

작성자명은 게시글 조회 시 `member` 테이블과 JOIN하여 `member.nickname` 값을 가져오도록 했습니다.  
이 방식은 회원이 닉네임을 변경하더라도 기존 게시글의 작성자명이 자동으로 최신 값으로 반영된다는 장점이 있습니다.

또한 회원 권한은 `role` 컬럼으로 관리하여 일반 사용자는 `USER`, 관리자는 `ADMIN`으로 구분합니다.

회원 삭제는 데이터를 완전히 제거하는 hard delete 방식이 아니라, `deleted` 컬럼을 활용한 soft delete 방식으로 구현했습니다.  
회원이 탈퇴하더라도 해당 회원이 작성한 기존 게시글의 이력과 작성자 정보는 유지되어야 하므로, 회원 데이터를 물리적으로 삭제하지 않고 삭제 여부만 변경하도록 처리했습니다.

---

## 설계 결정 사항

### 관리자 권한 부여 방식

일반 회원가입 시 모든 사용자는 기본적으로 `USER` 권한을 가집니다.  
관리자 계정은 회원가입 후 DB에서 직접 `role` 값을 `ADMIN`으로 변경합니다.

회원가입 화면이나 요청 파라미터에서 `role` 값을 받으면 사용자가 임의로 `ADMIN` 권한을 요청할 수 있어 보안상 위험하므로, 서버에서는 회원가입 시 `role`을 항상 `USER`로 고정합니다.

```sql
UPDATE member SET role = 'ADMIN' WHERE email = 'admin@test.com';
```

### 회원 삭제 방식 (Soft Delete)

회원 정보는 DB에서 물리적으로 삭제하지 않고, `deleted` 컬럼을 이용해 soft delete 방식으로 처리합니다.

회원이 탈퇴하더라도 해당 회원이 작성한 기존 게시글의 이력과 작성자 정보는 유지되어야 합니다.  
회원 데이터를 완전히 삭제하면 게시글 조회 시 작성자 정보가 사라지거나 데이터 정합성이 깨질 수 있으므로, 상태 값만 변경하는 방식을 선택했습니다.

```sql
UPDATE member SET deleted = 1 WHERE email = 'user@test.com';
```

로그인 및 회원 조회 시에는 `deleted = 0`인 회원만 처리하여 탈퇴한 회원은 서비스 이용이 불가능하도록 제한합니다.

---

### 인터셉터 도입 및 세션 키 공통화

초기 개발 시 각 Controller 메서드마다 아래와 같은 로그인 체크 코드가 반복적으로 작성되어 있었습니다.

```java
Long memberId = (Long) session.getAttribute("memberId");
if (memberId == null) return "redirect:/login";
```

이 방식은 Controller가 늘어날수록 동일한 코드가 중복되고, 세션 키 이름을 문자열로 직접 작성하다 보니 오타나 이름 불일치로 인한 `NullPointerException`이 반복적으로 발생했습니다.

이를 해결하기 위해 두 가지를 도입했습니다.

**① SessionConst 상수 클래스**

세션에서 사용하는 키 이름을 상수로 일원화하여 오타와 불일치를 방지했습니다.

```java
public class SessionConst {
    public static final String MEMBER_ID = "memberId";
    public static final String EMAIL     = "email";
    public static final String NICKNAME  = "nickname";
    public static final String ROLE      = "role";
}
```

**② LoginInterceptor**

모든 요청이 Controller에 도달하기 전에 인터셉터에서 로그인 여부를 일괄 검사하도록 변경했습니다.  
로그인이 필요 없는 경로(`/`, `/login`, `/signup` 등)는 제외 처리했습니다.

```
모든 요청
    ↓
LoginInterceptor (로그인 여부 체크)
    ↓                    ↓
로그인 O            로그인 X
    ↓                    ↓
Controller          redirect:/login
```

이 구조로 변경한 이후 Controller에서 세션 체크 코드가 완전히 제거되었고, 세션 키 관련 오류가 발생하지 않게 되었습니다.
--- 

## 구현 기능

### 1. 회원가입

회원 가입 시 `member` 테이블에 사용자 정보가 저장됩니다.

이메일, 닉네임, 비밀번호, 주소 정보를 입력받으며, 비밀번호는 암호화하여 저장합니다.
--- 

### 2. 로그인

로그인 시 `member.email`과 `password`를 기준으로 사용자를 인증합니다.

로그인 성공 후 세션에 `memberId`, `nickname`, `email` 정보를 저장하여 이후 기능에서 사용자 식별에 활용합니다.

작성자명은 직접 저장하지 않고, 세션에 저장된 `memberId`를 `board.member_id`에 저장합니다.
---

### 3. 게시글 작성

게시글 작성 시 `board` 테이블에 게시글 정보가 저장됩니다.

작성자명은 직접 저장하지 않고, 세션에 저장된 `memberId`를 `board.member_id`에 저장합니다.
--- 

### 4. 게시글 목록 조회

게시글 목록은 `board` 테이블과 `member` 테이블을 JOIN하여 조회합니다.

로그인한 모든 회원이 전체 게시글을 조회할 수 있으며, 작성자명은 `member.nickname`으로 표시됩니다.

게시글은 최신순으로 정렬되며, 공지사항은 일반 게시글보다 상단에 노출됩니다.
--- 

### 5. 게시글 상세 조회

게시글 상세 화면에서는 게시글 내용, 작성일, 수정일, 조회수, 카테고리, 첨부파일 정보를 함께 확인할 수 있습니다.

작성자 정보는 `member` 테이블과 조인하여 출력합니다.
--- 

### 6. 게시글 수정

게시글 수정은 작성자 본인만 가능하도록 구현했습니다.

세션의 `memberId`와 게시글의 `board.member_id`를 비교하여 접근을 제한합니다.
--- 

### 7. 게시글 삭제

게시글 삭제 역시 작성자 본인만 가능하도록 구현했습니다.

삭제 후에는 목록 화면으로 이동합니다.
--- 

### 8. 게시글 페이지네이션

게시글 수가 많아지는 경우를 대비해 목록 화면에 페이지네이션을 적용했습니다.

`PageDto`를 사용하여 현재 페이지와 페이지 크기를 관리하고, `LIMIT`와 `OFFSET`을 활용해 필요한 데이터만 조회합니다.

게시글 번호는 조회 시점에 계산하여, 삭제 후에도 번호가 자연스럽게 재정렬되도록 구성했습니다.
--- 

### 9. 닉네임 자동 반영

`board` 테이블에는 작성자명을 직접 저장하지 않고 `member_id`만 저장합니다.

따라서 게시글 조회 시마다 `member` 테이블의 `nickname`을 가져오므로, 회원이 닉네임을 수정하면 기존 게시글에도 최신 닉네임이 반영됩니다.
--- 

### 10. 공지사항 구분

`board.is_notice` 컬럼을 통해 일반 게시글과 공지사항을 구분할 수 있도록 설계했습니다.

목록 조회 시 공지사항이 먼저 노출되도록 정렬 기준에 반영할 수 있습니다.
--- 

### 11. 회원 삭제 방식

회원 삭제는 데이터베이스에서 회원 정보를 완전히 삭제하는 hard delete 방식이 아니라, `deleted` 컬럼을 활용한 soft delete 방식으로 구현했습니다.

회원이 탈퇴하더라도 기존 게시글의 작성 이력과 작성자 정보는 유지되어야 하므로, 회원 데이터를 물리적으로 삭제하면 게시글 조회 시 데이터 정합성이 깨질 수 있습니다.

이를 방지하기 위해 `member` 테이블에 `deleted` 컬럼을 두고, 회원 탈퇴 시 해당 값을 `1`로 변경하도록 처리했습니다.

또한 로그인 및 회원 조회 시에는 `deleted = 0`인 회원만 대상으로 처리하여, 탈퇴한 회원은 서비스를 이용할 수 없도록 제한했습니다.

---

### 12. 인터셉터 기반 로그인 검증

`LoginInterceptor`를 도입하여 로그인이 필요한 모든 페이지에 대한 접근 제어를 일원화했습니다.

로그인하지 않은 사용자가 보호된 경로에 접근하면 자동으로 로그인 페이지로 리다이렉트됩니다.

세션 키는 `SessionConst` 클래스에 상수로 정의하여 전체 코드에서 일관성 있게 사용합니다.

---

## 설계 특징

회원 정보와 게시글 정보를 분리하여 데이터 중복을 줄였습니다.

작성자명을 게시글 테이블에 직접 저장하지 않아 닉네임 변경에 유연하게 대응할 수 있습니다.

`member`와 `board`의 1:N 관계를 통해 한 명의 회원이 여러 게시글을 작성할 수 있도록 설계했습니다.

목록 조회 시 페이지네이션을 적용하여 대용량 데이터에서도 안정적으로 동작하도록 구성했습니다.

회원 삭제는 soft delete 방식으로 처리하여, 회원 탈퇴 이후에도 기존 게시글의 데이터 정합성을 유지할 수 있도록 했습니다.

---

## 🐛 트러블슈팅

> 개발 중 겪은 문제와 해결 과정을 날짜순으로 기록합니다.

---

<details>
<summary><b>[2026-04-29] 이메일 중복 체크 - 컨트롤러/서비스 역할 분리 미숙</b></summary>

<br>

### 📌 문제 상황
- 회원가입 시 이메일 중복 검증을 컨트롤러에서 직접 처리하려 했으나 구조가 잘못됨

### 🔍 원인
- 컨트롤러 / 서비스 / 매퍼의 역할 분리를 이해하지 못했음
- 서비스 메서드의 반환값을 어떻게 활용하는지 몰랐음

### ✅ 해결

```
컨트롤러  →  이메일/비밀번호 수신
   ↓
서비스    →  countByEmail()로 중복 조회
             중복이면 false 반환
             아니면 insert 후 true 반환
   ↓
컨트롤러  →  true/false 받아서 alert 분기 처리
```

```java
// Service
public boolean join(String email, String password) {
    if (homeMapper.countByEmail(email) > 0) {
        return false;
    }
    homeMapper.insertMember(user);
    return true;
}

// Controller
boolean result = homeService.join(email, password);
if (!result) {
    return "<script>alert('이미 사용 중인 이메일입니다.');</script>";
} else {
    return "<script>alert('회원가입이 완료되었습니다.');</script>";
}
```

### 💡 배운 점
- 비즈니스 로직은 서비스에서 처리하고, 컨트롤러는 결과만 받아서 응답한다
- 서비스 반환값을 `boolean`으로 설계하면 컨트롤러 분기가 깔끔해진다

</details>

---

<details>
<summary><b>[2026-04-29] 비밀번호 암호화 - spring-security-config 의존성 충돌</b></summary>

<br>

### 📌 문제 상황
- 비밀번호가 DB에 평문으로 저장되어 보안 문제 발생
- BCrypt 암호화를 위해 `spring-security-config`를 추가했더니 앱 실행 시 `SecurityAutoConfiguration` 관련 에러 발생하며 서버가 뜨지 않음

### 🔍 원인
- `spring-security-config`는 BCrypt 외에도 Spring Security 전체를 포함하고 있어서 모든 URL에 인증을 요구하는 자동 설정이 켜졌기 때문

### ✅ 해결
- `build.gradle`에서 `spring-security-config` → `spring-security-crypto`로 교체
- BCrypt 암호화 기능만 가져오고 Security 자동 설정은 켜지지 않음

### 💡 배운 점
- `spring-security-config`(Security 전체)와 `spring-security-crypto`(암호화만)의 차이를 이해함
- BCrypt는 단방향 암호화라 DB가 노출돼도 원래 비밀번호를 알 수 없음
- 필요한 기능만 정확히 골라서 의존성을 추가해야 한다

</details>

---

<details>
<summary><b>[2026-04-29] 회원가입 후 페이지 이동 안됨 - @ResponseBody 오용</b></summary>

<br>

### 📌 문제 상황
- 회원가입 요청 시 DB에는 회원 데이터가 정상적으로 INSERT 되었지만, 성공 후 로그인 페이지로 화면 이동이 되지 않음

### 🔍 원인
- 회원가입 처리 Controller 메서드에 `@ResponseBody`가 붙어 있었음
- `@ResponseBody`가 있으면 반환값인 `"main"` 또는 `"signup"`을 View 이름으로 해석하지 않고 문자열 그대로 HTTP 응답 본문에 출력함
- 추가로 `if (!result)` 조건이 반대로 작성되어 성공/실패 처리 흐름도 잘못되어 있었음

### ✅ 해결
- 페이지 이동이 필요한 메서드에서 `@ResponseBody` 제거
- 조건문을 `if (result)`로 수정
- 회원가입 성공 시 `redirect:/`로 변경

### 💡 배운 점
- `@ResponseBody`는 HTML 페이지 이동이 아니라 문자열이나 JSON 데이터를 직접 응답할 때 사용한다
- Service 메서드의 반환값 의미를 명확히 이해한 뒤 Controller에서 조건을 올바르게 처리해야 한다

</details>

---

<details>
<summary><b>[2026-04-29] 로그인 처리 오류 - Mapper 반환 타입 불일치</b></summary>

<br>

### 📌 문제 상황
- 회원가입한 계정으로 로그인을 시도했지만 로그인이 정상적으로 처리되지 않음

### 🔍 원인
- Mapper의 로그인 조회 메서드는 `UserDto`를 반환하도록 작성되어 있었지만, Service에서는 반환값을 `User` 타입으로 강제 형변환하고 있었음
- Mapper XML에 로그인 처리를 위한 회원 조회 `SELECT` 쿼리가 누락되어 있었음

### ✅ 해결
- Service에서 `User`가 아닌 `UserDto`로 회원 정보를 받도록 수정
- Mapper XML에 이메일 기준으로 회원 정보를 조회하는 `findUserLogin` 쿼리 추가
- `passwordEncoder.matches()`를 사용해 입력한 비밀번호와 DB의 암호화된 비밀번호를 비교

### 💡 배운 점
- MyBatis Mapper의 반환 타입과 Service에서 받는 타입은 반드시 일치해야 한다
- 로그인 기능에서는 먼저 DB에서 회원 정보를 조회한 뒤, 암호화된 비밀번호는 `passwordEncoder.matches()`로 비교해야 한다

</details>

---

<details>
<summary><b>[2026-04-29] 회원정보 수정 시 세션 key 불일치로 사용자 정보 조회 실패</b></summary>

<br>

### 📌 문제 상황
- 회원정보 수정 화면에 접근하거나 수정 요청을 처리할 때 로그인한 사용자의 정보를 정상적으로 가져오지 못함

### 🔍 원인
- 로그인 시 세션에 저장한 key 값과, 회원정보 수정 기능에서 세션 값을 꺼낼 때 사용하는 key 값이 불일치

```java
// 로그인 시 저장
httpSession.setAttribute("email", userDto.getEmail());

// 수정 시 조회 (key 이름이 다름)
String email = (String) httpSession.getAttribute("loginEmail"); // null 반환
```

### ✅ 해결
- 로그인 시 세션에 저장하는 key와 꺼내는 key를 `"email"`로 통일

```java
// 로그인 성공 시
httpSession.setAttribute("email", loginUser.getEmail());

// 회원정보 수정 시
String email = (String) httpSession.getAttribute("email");
```

### 💡 배운 점
- 세션을 사용하는 기능에서는 `setAttribute()`와 `getAttribute()`의 key 이름이 반드시 일치해야 한다
- 로그인, 로그아웃, 회원정보 수정처럼 세션을 함께 사용하는 기능에서는 세션 key 이름을 하나로 통일해서 관리하는 것이 중요하다

</details>

---

<details>
<summary><b>[2026-04-29] 회원정보 삭제 후 세션 미삭제 문제</b></summary>

<br>

### 📌 문제 상황
- 회원정보 삭제 후에도 로그인 세션이 남아 있어 삭제된 회원이 계속 로그인된 상태처럼 처리될 수 있었음

### 🔍 원인
- DB에서는 회원 정보가 삭제되었지만, 로그인 시 저장한 세션 정보를 따로 삭제하지 않았음

### ✅ 해결
- 회원 삭제 처리 후 `httpSession.invalidate()` 호출하여 세션 무효화

```java
@PostMapping("/user/delete")
public String deleteUser(HttpSession httpSession) {
    String email = (String) httpSession.getAttribute("email");
    if (email == null) return "redirect:/";

    userService.deleteByEmail(email);
    httpSession.invalidate();
    return "redirect:/";
}
```

### 💡 배운 점
- 회원 삭제 후에는 DB 데이터만 삭제하는 것이 아니라, 로그인 상태를 유지하고 있는 세션도 함께 삭제해야 한다
- `getAttribute()`는 세션 값을 사용할 때, `invalidate()`는 세션을 삭제할 때 사용한다

</details>

---

<details>
<summary><b>[2026-04-29] 로그아웃 시 불필요한 세션 조회 코드 제거</b></summary>

<br>

### 📌 문제 상황
- 로그아웃 메서드 안에 세션에서 이메일 값을 조회하는 불필요한 코드가 남아 있었음

### 🔍 원인
- 로그아웃은 세션을 무효화하는 기능인데, 로그인 사용자 정보를 확인하던 코드가 그대로 남아 있었음

### ✅ 해결
- `String email = (String) httpSession.getAttribute("email");` 코드 제거
- `httpSession.invalidate()`만 사용하여 세션 전체 무효화

### 💡 배운 점
- 로그아웃 처리에서는 세션 값을 조회할 필요 없이 세션을 무효화하면 된다

</details>

---

<details>
<summary><b>[2026-05-08] 회원정보 수정 시 파라미터 전달 안됨 - HTML name 속성 불일치</b></summary>

<br>

### 📌 문제 상황
- 회원정보 수정 시 `email`, `password` 값이 서버로 전달되지 않음

### 🔍 원인
- HTML form의 `input name` 속성값이 Controller의 `@RequestParam` 변수명과 불일치
- 예: `name="userEmail"` → `@RequestParam String email`로 받으려 해서 `null` 발생

### ✅ 해결
- `input name` 속성값을 Controller `@RequestParam` 변수명과 동일하게 맞춤

### 💡 배운 점
- HTML `name` 속성 ↔ `@RequestParam` 변수명은 반드시 일치해야 한다
- 설계 단계에서 파라미터 이름을 미리 정의해두면 혼동 방지 가능

</details>

---

<details>
<summary><b>[2026-05-16] 게시글 작성 기능 - MyBatis Mapper 인식 오류 (application.yml 문법 오류)</b></summary>

<br>

### 📌 문제 상황
- 게시글 작성 기능 구현 중 `UnsatisfiedDependencyException` 발생
- `Cannot resolve reference to bean 'sqlSessionTemplate'` 오류
- BoardController → BoardService → BoardMapper 의존성 주입 실패

### 🔍 원인
- `application.yml` 파일의 YAML 문법 오류
- `prefix=classpath:/templates/` 부분에서 `=` 기호 사용 (YAML에서는 `:` 사용해야 함)
- 잘못된 문법으로 인해 MyBatis 설정이 로드되지 않아 `sqlSessionTemplate` 빈 생성 실패

### ✅ 해결

```yaml
# 수정 전
spring:
  thymeleaf:
    prefix=classpath:/templates/

# 수정 후
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: false
```

### 💡 배운 점
- YAML 파일은 `key: value` 형식을 사용하며 `=` 기호를 사용하면 안 된다
- YAML은 들여쓰기에 민감하므로 스페이스 2칸 또는 4칸으로 일관성 있게 작성해야 한다
- Spring Boot 설정 파일 오류는 의존성 주입 실패로 이어질 수 있다

</details>

---

<details>
<summary><b>[2026-05-16] 게시글 작성 시 작성자 정보 누락 - 세션에 memberId 미저장</b></summary>

<br>

### 📌 문제 상황
- 로그인은 성공했으나 게시글 작성 페이지 접근 시 로그인 페이지로 리다이렉트됨
- 세션에서 `memberId`를 찾을 수 없다는 로그 확인

### 🔍 원인
- 로그인 성공 시 세션에 `email`만 저장하고 `memberId`와 `nickname`을 저장하지 않음
- `board.member_id` 컬럼(FK)에 작성자 정보를 저장하려면 `memberId`가 필수인데 세션에 없음

### ✅ 해결

```java
// 로그인 성공 시 세션에 필요한 정보 모두 저장
session.setAttribute("memberId", loginMember.getId());
session.setAttribute("nickname", loginMember.getNickname());
session.setAttribute("email", loginMember.getEmail());
```

### 💡 배운 점
- 로그인 시 세션에는 사용자 식별자(`memberId`)를 반드시 저장해야 한다
- 세션 설계는 초기 로그인 단계에서 신중하게 해야 나중에 수정 비용이 줄어든다

</details>

---

<details>
<summary><b>[2026-05-16] BoardMapper.xml 파일 인식 실패 - 잘못된 파일 위치</b></summary>

<br>

### 📌 문제 상황
- `BoardMapper` 인터페이스는 정상 인식되나 XML 파일의 SQL 쿼리가 실행되지 않음
- `BindingException: Invalid bound statement (not found)` 오류 발생

### 🔍 원인
- `BoardMapper.xml` 파일이 `src/main/java/` 하위에 생성됨
- MyBatis는 `src/main/resources/mapper/` 경로에서 XML 파일을 찾음
- `application.yml`의 `mybatis.mapper-locations: classpath:mapper/*.xml` 설정과 실제 파일 위치 불일치

### ✅ 해결
- 파일을 `src/main/resources/mapper/BoardMapper.xml`로 이동

```xml
<mapper namespace="com.practice.logincrud.board.BoardMapper">
    <insert id="save" parameterType="com.practice.logincrud.board.BoardDto">
        INSERT INTO board (title, content, created_at, member_id)
        VALUES (#{title}, #{content}, NOW(), #{memberId})
    </insert>
</mapper>
```

### 💡 배운 점
- MyBatis XML 파일은 반드시 `src/main/resources` 하위에 위치해야 한다
- `src/main/java`는 Java 소스 코드만, `src/main/resources`는 설정 파일 및 리소스 파일용
- Mapper 인터페이스의 `namespace`와 XML의 `namespace`가 정확히 일치해야 한다 (패키지명 포함)

</details>

---

<details>
<summary><b>[2026-05-16] 게시판 전체 조회 - Thymeleaf 템플릿 파싱 오류 (필드명 대소문자 불일치)</b></summary>

<br>

### 📌 문제 상황
- 게시판 전체 조회 기능 구현 후 `TemplateInputException` 발생
- 게시글 목록은 정상 조회되지만 Thymeleaf 파싱 단계에서 오류 발생

### 🔍 원인
- `BoardDto`의 필드명이 `nickname`으로 선언되어 있었으나 `home.html`에서 `${board.nickName}`으로 접근
- Thymeleaf는 대소문자를 구분하므로 필드를 찾지 못해 파싱 오류 발생

### ✅ 해결
- `BoardDto.java`의 필드명을 `nickName`으로 통일
- `MemberController`의 세션 key도 `nickName`으로 통일
- 로그인 성공 시 `return "home"` → `return "redirect:/home"`으로 변경

### 💡 배운 점
- Thymeleaf는 대소문자를 엄격하게 구분하므로 필드명 일관성이 중요하다
- `return "home"`은 컨트롤러에서 직접 뷰 렌더링, `return "redirect:/home"`은 새로운 GET 요청 생성

</details>

---

<details>
<summary><b>[2026-05-19] 게시글 수정 후 404 오류 - 리다이렉트 경로 불일치</b></summary>

<br>

### 📌 문제 상황
- 수정 페이지에서 '수정 완료' 버튼 클릭 시 `404 Not Found (No static resource board/19)` 에러 발생
- DB 수정 로직은 정상이나 수정 후 상세 페이지로 리다이렉트되는 과정에서 경로를 찾지 못함

### 🔍 원인
- 실제 상세 조회 주소는 `/board/view/{id}`였으나, 수정 처리 후 `return "redirect:/board/" + id;`를 사용하여 존재하지 않는 주소로 요청을 보냄
- 요청한 URL을 처리할 컨트롤러 메서드가 없으면 스프링은 이를 정적 파일로 오해하여 `static` 폴더를 탐색하고, 파일이 없으면 `No static resource` 에러를 던짐

### ✅ 해결
- 리턴 값을 실제 상세 페이지 주소인 `return "redirect:/board/view/" + id;`로 수정
- `boardDto.setId(id);`를 명시하여 URL의 ID 값이 DTO에 담겨 정확한 글이 수정되도록 보장

### 💡 배운 점
- `redirect:`는 파일명이 아닌 컨트롤러 주소를 호출하는 것이며, 주소 설계가 어긋나면 서버가 이를 정적 리소스로 오해할 수 있다
- RESTful API에서 자원의 상태를 변경할 때는 `POST`, 조회할 때는 `GET` 방식을 사용한다

</details>

---

<details>
<summary><b>[2026-05-19] 게시글 삭제 기능 - NullPointerException 및 404 오류</b></summary>

<br>

### 📌 문제 상황
- 삭제 버튼을 눌러도 기능이 정상적으로 동작하지 않음
- `NullPointerException: Cannot invoke "BoardDto.getMemberId()" because "board" is null` 발생
- 삭제 후 이동 경로가 맞지 않아 404 오류 발생

### 🔍 원인
- 삭제 폼의 요청 방식이 `GET`, 컨트롤러는 `POST` 방식으로 작성되어 불일치
- 삭제 전 게시글 조회 결과가 `null`일 수 있는데 null 체크 없이 `board.getMemberId()` 호출
- 삭제 후 이동 경로를 잘못 설정하여 존재하지 않는 페이지로 이동 시도

### ✅ 해결

```html
<!-- 폼 method를 POST로 수정 + 확인창 추가 -->
<form th:action="@{/board/delete/{id}(id=${board.id})}"
      method="post"
      onsubmit="return confirm('삭제하시겠습니까?');">
    <button type="submit">삭제</button>
</form>
```

```java
// null 체크 추가
BoardDto board = boardService.findById(id);
if (board == null) return "redirect:/home";

// 삭제 후 홈으로 이동
return "redirect:/home";
```

### 💡 배운 점
- 삭제 기능은 요청 방식과 컨트롤러 매핑을 함께 맞춰야 한다
- 조회 결과가 없을 가능성을 고려해 null 체크를 반드시 해야 한다
- 중요한 작업에는 사용자 확인 절차를 넣는 것이 좋다

</details>

---

<details>
<summary><b>[2026-05-19] 닉네임이 잠깐 표시되다가 "사용자"로 바뀌는 현상 - Thymeleaf와 JavaScript 충돌</b></summary>

<br>

### 📌 문제 상황
- 로그인 후 `/home` 페이지에서 닉네임이 잠깐 표시되었다가 곧바로 "사용자님 안녕하세요"로 바뀌는 현상 발생

### 🔍 원인
- 서버 렌더링(Thymeleaf)과 클라이언트 렌더링(JavaScript)의 충돌
- `board.js`에서 페이지 로드 후 `fetch('/api/user-info')`를 호출하여 사용자 정보를 다시 가져옴
- `/api/user-info` 메서드는 `email`만 반환하고 `nickName`은 반환하지 않음
- JS에서 `data.nickName`이 `undefined`가 되어 최종적으로 `'사용자'`로 덮어쓰게 됨

```javascript
// 문제 코드
const nickname = data.nickname || data.name || data.username || '사용자';
```

### ✅ 해결

```java
// API 응답에 nickName 추가
response.put("nickName", nickName != null ? nickName : "");
```

```javascript
// data.nickName을 우선 사용
const nickname = data.nickName || '사용자';
document.getElementById('nickname').textContent = nickname;
```

### 💡 배운 점
- 서버 렌더링과 클라이언트 렌더링이 동시에 존재할 때는 데이터 흐름을 명확히 해야 한다
- JS에서 DOM을 직접 수정할 경우 기존에 서버에서 전달한 값이 덮어쓰이지 않도록 주의해야 한다
- 사용자 정보 API를 만들 때는 필요한 모든 필드를 한 번에 반환하는 것이 좋다

</details>

---

<details>
<summary><b>[2026-06-02] 로그인 후 전체 유저 게시글 조회 안됨 - WHERE 조건 오류</b></summary>

<br>

### 📌 문제 상황
- 로그인 후 게시판 목록에서 **본인 게시글만 표시됨**
- 다른 유저가 작성한 게시글이 목록에 나타나지 않음
- 페이지네이션도 본인 글 기준으로만 계산되어 전체 글 수와 불일치

### 🔍 원인
- `BoardMapper.xml`의 쿼리에 `WHERE b.member_id = #{memberId}` 조건이 존재하여 로그인한 사용자의 게시글만 필터링되고 있었음
- JOIN은 작성자 닉네임을 가져오기 위한 목적이었으나, 필터링 조건까지 함께 적용된 것

```sql
-- ❌ 문제 코드
SELECT * FROM board b
INNER JOIN member m ON b.member_id = m.id
WHERE b.member_id = #{memberId}  -- 이 조건이 전체 조회를 막음
ORDER BY b.created_at DESC
```

### ✅ 해결
- WHERE 조건 제거 → 전체 유저 게시글 조회
- 카운트 쿼리도 동일하게 수정하여 페이지네이션 정확도 확보
- Service / Mapper 파라미터에서 `memberId` 제거

```sql
-- ✅ 수정 코드
SELECT * FROM board b
INNER JOIN member m ON b.member_id = m.id
ORDER BY b.is_notice DESC, b.created_at DESC
```

### 💡 배운 점
- JOIN은 작성자 닉네임을 가져오기 위한 것이지 필터링 목적이 아니다
- 목록 조회 쿼리와 카운트 쿼리의 WHERE 조건은 항상 동일하게 맞춰야 페이지네이션이 정확하다
- 전체 조회는 WHERE 조건 자체가 필요 없다

</details>

---

<details>
<summary><b>[2026-06-04] 댓글/대댓글 기능 구현 - 전체 구조 미숙지</b></summary>

<br>

### 📌 문제 상황
- 댓글/대댓글 기능을 처음 구현해야 했는데 DB 설계부터 HTML 렌더링까지 어떻게 구현해야 할지 전혀 몰랐음
- GPT에게 전체 코드를 받아서 붙여넣는 방식으로 시작하여 코드가 동작은 하지만 왜 동작하는지 이해하지 못한 상태였음

### 🔍 원인
- `parent_id` 개념을 처음 접함
- 댓글과 대댓글이 같은 테이블에 저장된다는 것을 몰랐음
- DB → Mapper → Service → Controller → HTML 전체 흐름을 이해하지 못했음

### ✅ 해결
- 댓글/대댓글은 같은 `comment` 테이블에 저장하고 `parent_id`로 구분하는 구조를 학습

```sql
-- parent_id가 NULL  → 댓글
-- parent_id가 숫자  → 해당 댓글의 대댓글
id=1, parent_id=NULL  → 댓글
id=3, parent_id=1     → id=1의 대댓글
```

- Service에서 DB의 평평한 리스트를 트리 구조로 변환하는 로직이 핵심임을 이해

```java
// 부모(댓글)만 뽑기
List<CommentDto> parents = all.stream()
    .filter(c -> c.getParentId() == null)
    .collect(Collectors.toList());

// 각 댓글에 대댓글 붙이기
for (CommentDto parent : parents) {
    List<CommentDto> children = all.stream()
        .filter(c -> parent.getId().equals(c.getParentId()))
        .collect(Collectors.toList());
    parent.setChildren(children);
}
```

### 💡 배운 점
- 댓글과 대댓글은 별도 테이블이 아니라 `parent_id` 하나로 같은 테이블에서 구분한다
- Service 계층에서 평평한 리스트를 트리 구조로 변환하는 역할을 담당한다
- 코드를 받아서 쓰는 것보다 각 계층이 왜 이 구조인지 이해하는 것이 중요하다

</details>

---

<details>
<summary><b>[2026-06-04] 댓글 수정/삭제 버튼 미노출 - Long 타입 비교 오류</b></summary>

<br>

### 📌 문제 상황
- 본인이 작성한 댓글에도 수정/삭제 버튼이 표시되지 않거나, 반대로 모든 사람에게 버튼이 보이는 오류 발생

### 🔍 원인
- Thymeleaf에서 `==` 연산자로 `Long` 타입을 비교하면 값이 아닌 객체 주소를 비교하게 됨
- 값이 같아도 다른 객체이면 `false`가 반환될 수 있음

```html
<!-- ❌ 잘못된 코드 -->
th:if="${comment.memberId == loginMemberId}"
```

### ✅ 해결

```html
<!-- ✅ 올바른 코드 -->
th:if="${comment.memberId.toString() == loginMemberId.toString()}"
```

### 💡 배운 점
- Java의 `Long` 타입은 `==`으로 비교하면 안 되고 `.toString()` 또는 `.equals()`를 사용해야 한다
- Thymeleaf에서 숫자 타입 비교 시 `.toString()`으로 문자열 변환 후 비교하면 값 기준으로 정확하게 동작한다

</details>

---

<details>
<summary><b>[2026-06-04] 댓글 권한 조건 미동작 - model.addAttribute 변수명 불일치</b></summary>

<br>

### 📌 문제 상황
- Controller에서 넘긴 로그인 사용자 정보가 HTML에서 인식되지 않아 권한 조건이 정상 동작하지 않음

### 🔍 원인
- Controller에서 `model.addAttribute("loginMemberId", memberId)`로 저장했으나
- HTML에서 `${memberId}`로 다른 이름을 사용하여 값이 `null`로 처리됨

```java
// Controller
model.addAttribute("loginMemberId", memberId);
```

```html
<!-- ❌ 잘못된 코드 - 이름이 다름 -->
th:if="${memberId == ...}"
```

### ✅ 해결

```html
<!-- ✅ addAttribute 이름과 동일하게 -->
th:if="${loginMemberId == ...}"
```

### 💡 배운 점
- `model.addAttribute("이름", 값)`에서 지정한 이름과 HTML의 `${이름}`이 정확히 일치해야 한다
- 세션 key, addAttribute 이름, HTML 변수명은 프로젝트 전체에서 일관성 있게 관리해야 한다

</details>

---

<details>
<summary><b>[2026-06-04] 댓글 수정 버튼 CSS 미적용 - 클래스명 불일치</b></summary>

<br>

### 📌 문제 상황
- 댓글 수정 버튼에 CSS 스타일이 적용되지 않음

### 🔍 원인
- HTML에서 사용한 클래스명과 CSS에 정의된 클래스명이 서로 달랐음

```html
<!-- HTML -->
<button class="btn-edit-comment">수정</button>
```

```css
/* CSS */
.edit-comment-btn { ... }  /* 이름이 다름 */
```

### ✅ 해결
- HTML과 CSS 클래스명을 `edit-comment-btn`으로 통일

```html
<!-- ✅ -->
<button class="edit-comment-btn">수정</button>
```

```css
/* ✅ */
.edit-comment-btn { ... }
```

### 💡 배운 점
- HTML 클래스명과 CSS 선택자는 대소문자까지 정확히 일치해야 스타일이 적용된다
- 클래스명을 작성할 때 HTML과 CSS를 동시에 확인하는 습관이 필요하다

</details>

---

<details>
<summary><b>[2026-06-04] 대댓글 삭제 오작동 - th:value 중복 작성</b></summary>

<br>

### 📌 문제 상황
- 대댓글 삭제 시 잘못된 ID 값이 서버로 전달되어 정상적으로 삭제되지 않음

### 🔍 원인
- 대댓글 삭제 form의 `input` 태그에 `th:value` 속성이 두 개씩 중복 작성되어 있었음

```html
<!-- ❌ 잘못된 코드 -->
<input type="hidden" name="id" th:value="${reply.id}" th:value="${board.id}">
<input type="hidden" name="boardId" th:value="${board.id}" th:value="${board.id}">
```

### ✅ 해결

```html
<!-- ✅ 각각 하나씩만 -->
<input type="hidden" name="id" th:value="${reply.id}">
<input type="hidden" name="boardId" th:value="${board.id}">
```

### 💡 배운 점
- 같은 속성을 하나의 태그에 두 번 작성하면 예상치 못한 동작이 발생할 수 있다
- hidden input 작성 시 `name`과 `th:value`가 의도한 값을 담고 있는지 반드시 확인해야 한다

</details>

---

<details>
<summary><b>[2026-06-04] 댓글 실수 삭제 방지 - 삭제 확인 처리 미흡</b></summary>

<br>

### 📌 문제 상황
- 삭제 버튼을 실수로 눌렀을 때 확인 절차 없이 바로 삭제되는 문제

### 🔍 원인
- 댓글/대댓글 삭제 form에 사용자 확인 절차가 없었음

### ✅ 해결
- 댓글/대댓글 삭제 form 태그에 `onsubmit` 속성 추가

```html
<!-- 댓글 삭제 -->
<form th:action="@{/comment/delete}" method="post"
      th:if="${comment.memberId.toString() == loginMemberId.toString() or isAuthor or isAdmin}"
      onsubmit="return confirm('댓글을 삭제하시겠습니까?');">
    <input type="hidden" name="id" th:value="${comment.id}">
    <input type="hidden" name="boardId" th:value="${board.id}">
    <button type="submit" class="btn-comment-delete">삭제</button>
</form>

<!-- 대댓글 삭제 -->
<form th:action="@{/comment/delete}" method="post"
      th:if="${reply.memberId.toString() == loginMemberId.toString() or isAuthor or isAdmin}"
      onsubmit="return confirm('댓글을 삭제하시겠습니까?');">
    <input type="hidden" name="id" th:value="${reply.id}">
    <input type="hidden" name="boardId" th:value="${board.id}">
    <button type="submit" class="btn-comment-delete">삭제</button>
</form>
```

### 💡 배운 점
- `onsubmit="return confirm('...')"` 에서 확인 클릭 시 `true`를 반환하여 form이 제출되고, 취소 클릭 시 `false`를 반환하여 form 제출이 중단된다
- 삭제처럼 되돌릴 수 없는 작업에는 반드시 사용자 확인 절차를 추가해야 한다

</details>
