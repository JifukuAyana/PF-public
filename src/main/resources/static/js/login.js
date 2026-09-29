document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");

    form.addEventListener("submit", function (e) {
        const loginId = document.getElementById("loginId").value.trim();
        const password = document.getElementById("password").value.trim();

        const loginIdError = document.getElementById("loginIdError");
        const passwordError = document.getElementById("passwordError");

        // エラー領域クリア
        loginIdError.textContent = "";
        passwordError.textContent = "";

        let hasError = false;

        // ✅ ユーザー名のバリデーション
        if (!loginId) {
            loginIdError.textContent = "ユーザー名を入力してください。";
            hasError = true;
        } else if (loginId.length > 254) {
            loginIdError.textContent = "ユーザー名は255文字未満で入力してください。";
            hasError = true;
        }

        // ✅ パスワードのバリデーション（長さ＋形式に分ける）
        if (!password) {
            passwordError.textContent = "パスワードを入力してください。";
            hasError = true;
        } else if (password.length < 8 || password.length > 32) {
            passwordError.textContent = "パスワードは8～32文字で入力してください。";
            hasError = true;
        } else if (!/^[a-zA-Z0-9_-]+$/.test(password)) {
            passwordError.textContent = "パスワードは半角英数字と_-のみ使用可能です。";
            hasError = true;
        }

        if (hasError) {
            e.preventDefault(); // フォーム送信中止
        }
    });
});