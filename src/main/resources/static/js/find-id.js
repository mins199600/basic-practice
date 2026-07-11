document.addEventListener("DOMContentLoaded", () => {
    const emailInput = document.getElementById("email");
    const codeInput = document.getElementById("code");
    const resultBox = document.getElementById("resultBox");
    const resultValue = document.getElementById("resultValue");

    window.sendCode = async function () {
        const email = emailInput.value.trim();
        if (!email) {
            alert("이메일을 입력해주세요.");
            return;
        }

        const res = await fetch("/find-id/send-code", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({ email })
        });

        const data = await res.json();
        alert(data.message);
    };

    window.verifyCode = async function () {
        const email = emailInput.value.trim();
        const code = codeInput.value.trim();

        if (!email || !code) {
            alert("이메일과 인증번호를 입력해주세요.");
            return;
        }

        const res = await fetch("/find-id/verify-code", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams({ email, code })
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
