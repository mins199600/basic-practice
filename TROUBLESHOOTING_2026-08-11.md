# 트러블슈팅 로그 — 2026-08-11
## AI 코딩테스트 "새 문제 받기" 중복 클릭 시 문제 2개 생성되는 버그 수정

### 배경

코딩테스트 페이지(`/codingtest`)에서 "새 문제 받기" 버튼을 클릭하면 Claude API가 새 문제를 생성하는데, 응답이 오는 데 수 초가 걸린다. 이 대기 시간 동안 화면이 그대로 멈춰 있어서, 사용자가 "안 눌렸나?" 하고 버튼을 한 번 더 클릭하는 일이 실제로 발생했고, 그 결과 같은 요청이 두 번 처리되어 코딩테스트 세션(문제)이 2개 생성됐다.

### 원인 — 폼 제출을 막는 코드가 없어서 생긴 중복 제출(Double Submit)

`/codingtest/start`는 AJAX가 아니라 일반 `<form method="post">`로 구현되어 있다.

```html
<!-- 수정 전: templates/codingtest/list.html -->
<form th:action="@{/codingtest/start}" method="post" class="codingtest-start-form">
    <select name="difficulty" required>...</select>
    <button type="submit" class="write-button">새 문제 받기</button>
</form>
```

버튼을 눌러도 비활성화되지 않기 때문에, 서버 응답(리다이렉트)이 오기 전까지는 몇 번이든 다시 클릭할 수 있다. 서버 쪽 흐름도 매 요청마다 무조건 새 세션을 만든다:

```java
// CodingTestService.java
public CodingTestSessionDto startSession(Long memberId, String difficulty) {
    String problemText = codingTestAiService.generateProblem(difficulty); // Claude API 동기 호출, 수 초 소요
    CodingTestSessionDto session = new CodingTestSessionDto();
    ...
    codingTestSessionMapper.insert(session); // 요청이 두 번 오면 insert도 두 번
    ...
}
```

즉 "느린 AI 호출(원인) + 중복 제출을 막는 안전장치 없음(방치된 결함)"이 겹쳐서 터진 버그다. AI 응답 속도 자체는 컨트롤하기 어려우므로, 중복 제출을 막는 쪽이 실질적인 해결책이다.

### 조치 — 제출 즉시 버튼을 비활성화 (클라이언트 사이드 가드)

`templates/codingtest/list.html`의 폼에 id를 부여하고, `submit` 이벤트가 발생하는 순간 버튼을 `disabled` 처리 + 안내 텍스트로 교체하도록 스크립트를 추가했다.

```html
<!-- 수정 후: templates/codingtest/list.html -->
<form th:action="@{/codingtest/start}" method="post" class="codingtest-start-form" id="startForm">
    <select name="difficulty" required>...</select>
    <button type="submit" class="write-button" id="startButton">새 문제 받기</button>
</form>

<script>
    document.getElementById('startForm').addEventListener('submit', function () {
        var btn = document.getElementById('startButton');
        btn.disabled = true;
        btn.textContent = 'AI가 문제를 만드는 중입니다...';
    });
</script>
```

첫 클릭 시점에 버튼이 물리적으로 비활성화되므로, 이후 아무리 다시 클릭해도 두 번째 요청 자체가 브라우저에서 발생하지 않는다.

### 검증

1. `/codingtest` 접속 → 난이도 선택 → "새 문제 받기" 클릭
2. 클릭 직후 버튼이 "AI가 문제를 만드는 중입니다..."로 바뀌며 비활성화되는지 확인
3. 응답을 기다리는 동안 버튼을 연타해도 개발자도구 Network 탭에 `/codingtest/start` 요청이 1건만 찍히는지 확인
4. 완료 후 `/codingtest` 목록에 세션이 정확히 1개만 생성됐는지 확인

### 남은 과제 (후속 조치 후보)

- 지금 조치는 "정상적인 브라우저에서 버튼을 두 번 클릭하는 경우"만 막는다. 새로고침 후 재제출, 네트워크 재시도 등 더 드문 경로까지 막으려면 서버 쪽에도 "회원당 진행 중(미완료) 세션이 있으면 새 세션 생성을 막는" 가드를 추가하는 게 근본적이다.
- 근본적으로는 AI 응답을 기다리는 동안 "멈춘 것처럼 보이는" UX 자체가 문제이므로, 로딩 스피너/진행 표시를 추가하면 재발 가능성이 더 낮아진다.
