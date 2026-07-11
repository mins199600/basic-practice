document.addEventListener("DOMContentLoaded", () => {
    const nicknameInput = document.getElementById("nickname");
    const codeInput = document.getElementById("code");
    const resultBox = document.getElementById("resultBox");
    const resultValue = document.getElementById("resultValue");

    window.sendCode = async function () {
        const nickname = nicknameInput.value.trim();
        if (!nickname) {
            alert("닉네임을 입력해주세요.");
            return;
        }

        const res = await fetch("/find-id/send-code", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({ nickname })
        });

        const data = await res.json();
        alert(data.message);
    };

    window.verifyCode = async function () {
        const nickname = nicknameInput.value.trim();
        const code = codeInput.value.trim();

        if (!nickname || !code) {
            alert("닉네임과 인증번호를 입력해주세요.");
            return;
        }

        const res = await fetch("/find-id/verify-code", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({ nickname, code })
        });

        const data = await res.json();

        if (data.success) {
            resultValue.textContent = data.email;
            resultBox.style.display = "flex";
        } else {
            alert(data.message);
        }
    };
});
