document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('updateForm');
    if (!form) {
        console.error("フォームが見つかりませんでした。IDが間違っている可能性があります。");
        return;
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();

        console.log("フォーム送信開始");

        const formData = new FormData(form);
        const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

        try {
            const response = await fetch('/set/updateUser', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': csrfToken
                }
            });

            const contentType = response.headers.get("content-type");

            if (!response.ok) {
                const errorText = await response.text();
                console.error("HTTPエラー: ", errorText);
                alert("サーバーエラーが発生しました。");
                return;
            }

            if (contentType && contentType.includes("application/json")) {
                const result = await response.json();

                if (result.success) {
                    alert("情報が更新されました！");
                    window.location.reload();
                } else {
                    alert("更新に失敗しました: " + result.message);
                }
            } else {
                const text = await response.text();
                console.warn("JSONではないレスポンス: ", text);
                alert("予期しないレスポンス形式です。");
            }
        } catch (error) {
            console.error("通信エラー:", error);
            alert("通信中にエラーが発生しました。");
        }
    });
});

