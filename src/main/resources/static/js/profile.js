document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('profileForm');
    if (!form) return;

    form.addEventListener('submit', function (event) {
        const profileImage = document.getElementById('profileImage').files[0];

        // JSでチェックするのは画像サイズ（2MB）だけ
        if (profileImage && profileImage.size > 2 * 1024 * 1024) {
            const errorEl = document.getElementById('profileImageError');
            errorEl.textContent = 'プロフィール画像は2MB以内で選択してください。';
            errorEl.style.display = 'block';
            event.preventDefault(); // 送信ブロック
        } else {
            const errorEl = document.getElementById('profileImageError');
            errorEl.textContent = '';
            errorEl.style.display = 'none';
        }

    });
});
