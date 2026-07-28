(function () {
    const chatWindow = document.getElementById('codingtest-window');
    const answerForm = document.getElementById('answer-form');
    const languageSelect = document.getElementById('language-select');
    const codeInput = document.getElementById('code-input');
    const submitBtn = document.getElementById('submit-btn');
    const solutionsBtn = document.getElementById('solutions-btn');

    function scrollToBottom() {
        chatWindow.scrollTop = chatWindow.scrollHeight;
    }

    function appendBubble(sender, label, content, isCode) {
        const bubble = document.createElement('div');
        bubble.className = sender === 'AI' ? 'codingtest-bubble codingtest-bubble--ai' : 'codingtest-bubble codingtest-bubble--user';

        if (label) {
            const labelEl = document.createElement('div');
            labelEl.className = 'codingtest-bubble-label';
            labelEl.textContent = label;
            bubble.appendChild(labelEl);
        }

        const contentEl = document.createElement('div');
        contentEl.className = isCode ? 'codingtest-bubble-content codingtest-bubble-content--code' : 'codingtest-bubble-content';
        contentEl.textContent = content;

        bubble.appendChild(contentEl);
        chatWindow.appendChild(bubble);
        scrollToBottom();
        return bubble;
    }

    function appendErrorBubble(message) {
        const bubble = document.createElement('div');
        bubble.className = 'codingtest-bubble codingtest-bubble--error';
        bubble.textContent = message;
        chatWindow.appendChild(bubble);
        scrollToBottom();
    }

    if (answerForm) {
        answerForm.addEventListener('submit', function (e) {
            e.preventDefault();

            const code = codeInput.value.trim();
            const language = languageSelect.value;
            if (!code) {
                return;
            }

            const languageLabel = languageSelect.options[languageSelect.selectedIndex].text;
            appendBubble('USER', '제출한 답안 (' + languageLabel + ')', code, true);
            codeInput.value = '';
            codeInput.disabled = true;
            submitBtn.disabled = true;
            submitBtn.textContent = 'AI가 첨삭 중...';

            fetch('/codingtest/sessions/' + CODINGTEST_SESSION_ID + '/submit', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ language: language, code: code })
            })
                .then(function (res) {
                    return res.json().then(function (data) {
                        return { ok: res.ok, data: data };
                    });
                })
                .then(function (result) {
                    if (!result.ok) {
                        appendErrorBubble(result.data.error || '오류가 발생했습니다.');
                        return;
                    }
                    appendBubble('AI', 'AI 첨삭', result.data.content, false);
                })
                .catch(function () {
                    appendErrorBubble('네트워크 오류로 응답을 받지 못했습니다. 다시 시도해주세요.');
                })
                .finally(function () {
                    codeInput.disabled = false;
                    submitBtn.disabled = false;
                    submitBtn.textContent = '답안 제출 및 첨삭받기';
                });
        });
    }

    if (solutionsBtn) {
        solutionsBtn.addEventListener('click', function () {
            solutionsBtn.disabled = true;
            solutionsBtn.textContent = '모범답안 생성 중...';

            fetch('/codingtest/sessions/' + CODINGTEST_SESSION_ID + '/solutions', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
                .then(function (res) {
                    return res.json().then(function (data) {
                        return { ok: res.ok, data: data };
                    });
                })
                .then(function (result) {
                    if (!result.ok) {
                        appendErrorBubble(result.data.error || '오류가 발생했습니다.');
                        return;
                    }
                    appendBubble('AI', '3개 언어 모범답안', result.data.content, true);
                })
                .catch(function () {
                    appendErrorBubble('네트워크 오류로 응답을 받지 못했습니다. 다시 시도해주세요.');
                })
                .finally(function () {
                    solutionsBtn.disabled = false;
                    solutionsBtn.textContent = '3개 언어 모범답안 보기';
                });
        });
    }

    scrollToBottom();
})();
