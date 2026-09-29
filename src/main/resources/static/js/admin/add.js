function toggleUserType() {
    const userType = document.querySelector('input[name="authority"]:checked').value;
    const generalFields = document.getElementById('generalFields');
    if (userType === '2') {
        generalFields.style.display = 'block';
    } else {
        generalFields.style.display = 'none';
    }
}

document.getElementById('userForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const errors = [];

    const name = document.getElementById('name').value.trim();
    if (name.length > 255) errors.push('名前は255文字以内で入力してください。');

    const email = document.getElementById('email').value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email) || email.length > 255) errors.push('メールアドレスを正しく入力してください (255文字以内)。');

    const password = document.getElementById('password').value;
    const passwordRegex = /^[a-zA-Z0-9_-]{8,10}$/;
    if (!passwordRegex.test(password)) errors.push('パスワードは8~10文字の半角英数字、アンダースコア(_)、ハイフン(-)のみ許可します。');

    if (document.querySelector('input[name="userType"]:checked').value === 'general') {
        const kana = document.getElementById('kana').value.trim();
        if (!/^[\u3040-\u309Fー]*$/.test(kana)) errors.push('ふりがなはひらがなのみ入力可能です。');
    }

    if (errors.length > 0) {
        alert(errors.join('\n'));
    } else {
        alert('登録が成功しました！');
    }
});
