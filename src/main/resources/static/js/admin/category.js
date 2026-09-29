document.addEventListener('DOMContentLoaded', async () => {
    const categoryContainer = document.getElementById('categoryContainer');
    const inquiryContainer = document.getElementById('inquiryContainer');
    const inquiryList = document.getElementById('inquiryList');

    // カテゴリー一覧を読み込む（問い合わせ件数も取得）
    async function loadCategories() {
        try {
            const response = await fetch('/api/categories');
            const categories = await response.json();

            categoryContainer.innerHTML = ''; // 一度クリア

            for (let category of categories) {
                // ✅ 件数取得を category.id ベースに変更
                const inquiryResponse = await fetch(`/api/inquiries/category?id=${category.id}`);
                let inquiryCount = 0;
                if (inquiryResponse.ok) {
                    const inquiries = await inquiryResponse.json();
                    inquiryCount = inquiries.length;
                }

                const div = document.createElement('div');
                div.classList.add('category-item');
                div.innerHTML = `
                    <p>${category.name} (${inquiryCount})</p>
                    <button class="edit-btn" onclick="editCategory('${category.id}', '${category.name}')">編集</button>
                    <button class="delete-btn" onclick="deleteCategory(${category.id})">削除</button>
                `;
                div.addEventListener('click', (e) => {
                    if (e.target === div || e.target.tagName === 'P') {
                        showInquiries(category.id); // ✅ IDベースで表示
                    }
                });
                categoryContainer.appendChild(div);
            }
        } catch (error) {
            console.error('カテゴリーデータの取得に失敗しました:', error);
        }
    }

    // 問い合わせモーダルを表示（ID指定）
    async function showInquiries(categoryId) {
        try {
            const response = await fetch(`/api/inquiries/category?id=${categoryId}`);
            const inquiries = await response.json();

            inquiryList.innerHTML = ''; // 一度リストをクリア

            if (inquiries.length === 0) {
                inquiryList.innerHTML = '<li>このカテゴリーには問い合わせがありません</li>';
            } else {
                inquiries.forEach(inquiry => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        ${inquiry.subject} - <span class="status ${inquiry.status}">${inquiry.status}</span>
                    `;
                    inquiryList.appendChild(li);
                });
            }

            inquiryContainer.classList.remove('hidden'); // モーダルを表示
        } catch (error) {
            console.error('問い合わせ一覧の取得に失敗しました:', error);
        }
    }

    // 問い合わせモーダルを閉じる
    window.closeInquiries = function () {
        inquiryContainer.classList.add('hidden');
    };

    // 詳細表示（別機能：カテゴリとは無関係）
    window.showDetail = async function (id) {
        try {
            const response = await fetch(`/api/inquiries/${id}`);
            const inquiry = await response.json();

            document.getElementById('detailCategory').innerText = `カテゴリ: ${inquiry.category}`;
            document.getElementById('detailText').innerText = `本文: ${inquiry.message}`;
            document.getElementById('statusSelect').value = inquiry.status;
            document.getElementById('statusSelect').setAttribute('data-id', inquiry.id);
            document.getElementById('detailContainer').classList.remove('hidden');
        } catch (error) {
            console.error('詳細情報の取得に失敗しました:', error);
            alert('詳細情報の取得に失敗しました');
        }
    };

    // ステータス更新
    window.updateStatus = async function () {
        const id = document.getElementById('statusSelect').getAttribute('data-id');
        const newStatus = document.getElementById('statusSelect').value;

        try {
            const response = await fetch(`/api/inquiries/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status: newStatus })
            });

            if (response.ok) {
                alert('ステータスを更新しました');
                document.getElementById('detailContainer').classList.add('hidden');
                loadCategories(); // 件数再取得
            } else {
                alert('ステータス更新に失敗しました');
            }
        } catch (error) {
            console.error('ステータス更新エラー:', error);
            alert('ステータス更新中にエラーが発生しました');
        }
    };

    // カテゴリー追加
    window.addCategory = async function () {
        const categoryInput = document.getElementById('newCategory');
        const categoryName = categoryInput.value.trim();

        if (!categoryName) {
            alert('カテゴリー名を入力してください');
            return;
        }

        try {
            const response = await fetch('/api/categories', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: categoryName }),
            });

            if (response.ok) {
                alert('カテゴリーを追加しました');
                categoryInput.value = '';
                loadCategories();
            } else {
                alert('カテゴリー名は255文字以内で入力してください。');
            }
        } catch (error) {
            console.error('カテゴリー追加エラー:', error);
        }
    };

    // カテゴリー編集（プロンプト）
    window.editCategory = async function (id, currentName) {
        const newName = prompt('新しいカテゴリー名を入力してください:', currentName);

        if (!newName) {
            alert('カテゴリー名を入力してください');
            return;
        }

        if (newName.length > 255) {
            alert('カテゴリー名は255文字以内で入力してください');
            return;
        }

        try {
            const response = await fetch(`/api/categories/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: newName })
            });

            if (response.ok) {
                alert('カテゴリーを更新しました');
                loadCategories();
            } else {
                alert('カテゴリー更新に失敗しました');
            }
        } catch (error) {
            console.error('カテゴリー更新エラー:', error);
        }
    };

    // カテゴリー削除
    window.deleteCategory = async function (id) {
        if (!confirm('本当にこのカテゴリーを削除しますか？')) return;

        try {
            const response = await fetch(`/api/categories/${id}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                alert('カテゴリーを削除しました');
                loadCategories();
            } else {
                alert('カテゴリー削除に失敗しました');
            }
        } catch (error) {
            console.error('カテゴリー削除エラー:', error);
        }
    };

    // 編集モーダルを開く（別方式）
    window.editCategory = function (id, currentName) {
        document.getElementById('editCategoryId').value = id;
        document.getElementById('editCategoryName').value = currentName;
        document.getElementById('editCategoryMessage').innerText = '';
        document.getElementById('editCategoryModal').classList.remove('hidden');
    };

    window.closeEditCategoryModal = function () {
        document.getElementById('editCategoryModal').classList.add('hidden');
        loadCategories();
    };

    window.submitCategoryUpdate = async function () {
        const id = document.getElementById('editCategoryId').value;
        const newName = document.getElementById('editCategoryName').value.trim();
        const messageDiv = document.getElementById('editCategoryMessage');

        if (!newName) {
            messageDiv.innerText = 'カテゴリー名を入力してください';
            messageDiv.style.color = 'red';
            return;
        }
        if (newName.length > 255) {
            messageDiv.innerText = 'カテゴリー名は255文字以内で入力してください';
            messageDiv.style.color = 'red';
            return;
        }

        try {
            const response = await fetch(`/api/categories/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: newName })
            });

            if (response.ok) {
                messageDiv.innerText = 'カテゴリーを更新しました';
                messageDiv.style.color = 'green';
            } else {
                const errorText = await response.text();
                messageDiv.innerText = errorText || 'カテゴリー更新に失敗しました';
                messageDiv.style.color = 'red';
            }
        } catch (error) {
            console.error('更新エラー:', error);
            messageDiv.innerText = '通信エラーが発生しました';
            messageDiv.style.color = 'red';
        }
    };

    // 初期表示
    loadCategories();
});
