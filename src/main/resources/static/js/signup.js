function toggleUserType() {
    const userType = document.querySelector('input[name="authority"]:checked')?.value;
    const generalFields = document.getElementById('generalFields');
    generalFields.style.display = (userType === '2') ? 'block' : 'none';
}

// ページ読み込み時とラジオ切替時に発動
document.addEventListener('DOMContentLoaded', () => {
    toggleUserType();
    document.querySelectorAll('input[name="authority"]').forEach(radio => {
        radio.addEventListener('change', toggleUserType);
    });
});
