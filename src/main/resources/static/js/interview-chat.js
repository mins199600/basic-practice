(function () {
    const chatWindow = document.getElementById('chat-window');
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const sendBtn = document.getElementById('chat-send-btn');
    const endBtn = document.getElementById('chat-end-btn');
    const endForm = document.getElementById('end-session-form');

    function scrollToBottom() {
        chatWindow.scrollTop = chatWindow.scrollHeight;
    }

    function appendBubble(sender, content) {
        const bubble = document.createElement('div');
        bubble.className = sender === 'AI' ? 'interview-bubble interview-bubble--ai' : 'interview-bubble interview-bubble--user';

        const contentEl = document.createElement('div');
        contentEl.className = 'interview-bubble-content';
        contentEl.textContent = content;

        bubble.appendChild(contentEl);
        chatWindow.appendChild(bubble);
        scrollToBottom();
        return bubble;
    }

    function appendErrorBubble(message) {
        const bubble = document.createElement('div');
        bubble.className = 'interview-bubble interview-bubble--error';
        bubble.textContent = message;
        chatWindow.appendChild(bubble);
        scrollToBottom();
    }

    if (chatForm) {
        chatForm.addEventListener('submit', function (e) {
            e.preventDefault();

            const content = chatInput.value.trim();
            if (!content) {
                return;
            }

            appendBubble('USER', content);
            chatInput.value = '';
            chatInput.disabled = true;
            sendBtn.disabled = true;
            sendBtn.textContent = 'AI가 첨삭 중...';

            fetch('/interview/sessions/' + SESSION_ID + '/messages', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: content })
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
                    appendBubble('AI', result.data.content);
                })
                .catch(function () {
                    appendErrorBubble('네트워크 오류로 응답을 받지 못했습니다. 다시 시도해주세요.');
                })
                .finally(function () {
                    chatInput.disabled = false;
                    sendBtn.disabled = false;
                    sendBtn.textContent = '답변 제출';
                    chatInput.focus();
                });
        });
    }

    if (endBtn) {
        endBtn.addEventListener('click', function () {
            if (confirm('면접을 종료하시겠습니까? 종료 후에는 이 세션에 답변을 이어갈 수 없습니다.')) {
                endForm.submit();
            }
        });
    }

    scrollToBottom();
})();
