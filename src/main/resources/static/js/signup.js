document.addEventListener("DOMContentLoaded", () => {
    // 서버 에러메시지(alert) 처리
    const serverErrorMessage = document.body.dataset.errorMessage;
    if (serverErrorMessage) {
        alert(serverErrorMessage);
    }

    let emailVerified = false;

    const form = document.getElementById("signupForm");
    const emailInput = document.getElementById("email");
    const codeInput = document.getElementById("code");

    window.sendCode = async function () {
        const email = emailInput.value.trim();
        if (!email) {
            alert("이메일을 입력해주세요.");
            return;
        }

        const res = await fetch("/email/send-code", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({ email })
        });

        const data = await res.json();
        alert(data.message);
        if (!data.success) emailVerified = false;
    };

    window.verifyCode = async function () {
        const email = emailInput.value.trim();
        const code = codeInput.value.trim();

        if (!email || !code) {
            alert("이메일과 인증번호를 입력해주세요.");
            return;
        }

        const res = await fetch("/email/verify-code", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({ email, code })
        });

        const data = await res.json();
        alert(data.message);
        emailVerified = data.success;
    };

    form.addEventListener("submit", (e) => {
        if (!emailVerified) {
            e.preventDefault();
            alert("이메일 인증을 먼저 완료해주세요.");
        }
    });
});
