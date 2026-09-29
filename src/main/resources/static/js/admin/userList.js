document.addEventListener("DOMContentLoaded", function () {
    const rowsPerPage = 5;
    let currentPage = 1;

    const tableRows = Array.from(document.querySelectorAll(".account-table tbody tr"));
    const paginationContainer = document.getElementById("pagination");

    function showPage(page) {
        tableRows.forEach((row, index) => {
            row.style.display = (index >= (page - 1) * rowsPerPage && index < page * rowsPerPage) ? "" : "none";
        });
    }

    function setupPagination() {
        paginationContainer.innerHTML = "";
        const pageCount = Math.ceil(tableRows.length / rowsPerPage);

        for (let i = 1; i <= pageCount; i++) {
            const button = document.createElement("button");
            button.textContent = i;
            button.classList.add("page-btn");
            if (i === currentPage) button.classList.add("active");

            button.addEventListener("click", function () {
                currentPage = i;
                showPage(currentPage);
                setupPagination();
            });

            paginationContainer.appendChild(button);
        }
    }

    function addEventListeners() {
		document.querySelectorAll(".edit-btn").forEach(button => {
		    button.addEventListener("click", function () {
		        const row = this.closest("tr");
		        const loginId = row.getAttribute("data-loginid");

                fetch(`/users/detail?loginId=${encodeURIComponent(loginId)}`)
                    .then(res => {
                        if (!res.ok) throw new Error("ユーザー情報取得に失敗しました");
                        return res.json();
                    })
                    .then(user => {
                        document.getElementById("originalLoginId").value = user.loginId;
                        document.getElementById("editName").value = user.loginId;
                        document.getElementById("editEmail").value = user.mail;
                        document.getElementById("editStatus").value = user.access;
                        document.getElementById("furigana").value = user.furigana || "";
                        document.getElementById("gender").value = user.gender || "1";
                        document.getElementById("age").value = user.age || "";
                        document.getElementById("introduction").value = user.introduction || "";

                        document.getElementById(user.authority === 1 ? "adminRadio" : "generalRadio").checked = true;
                        toggleUserType();

                        showEditModal();
                    })
                    .catch(error => {
                        console.error("エラー:", error);
                        alert("編集用データの取得に失敗しました");
                    });
            });
        });
        document.querySelectorAll(".delete-btn").forEach(button => {
            button.addEventListener("click", function () {
                const row = this.closest("tr");
                const loginId = row.getAttribute("data-loginid");


                if (!confirm(`ユーザー "${loginId}" を削除しますか？`)) return;

                fetch(`/users/${loginId}/delete`, {
                    method: "DELETE",
                    headers: { "Content-Type": "application/json" }
                })
                    .then(response => {
                        if (!response.ok) throw new Error("削除に失敗しました");
                        return response.text();
                    })
                    .then(message => {
                        alert(message);
                        row.remove();
                        setupPagination();
                    })
                    .catch(error => {
                        console.error("エラー:", error);
                        alert("削除に失敗しました");
                    });
            });
        });

        document.querySelectorAll(".status-btn").forEach(button => {
            button.addEventListener("click", function () {
                const row = this.closest("tr");
                const loginId = row.getAttribute("data-loginid");
                const statusCell = row.children[3];

                fetch(`/users/${loginId}/toggleAccess`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" }
                })
                    .then(response => {
                        if (!response.ok) throw new Error("ステータス変更に失敗しました");
                        return response.text();
                    })
                    .then(() => {
                        statusCell.textContent = statusCell.textContent === "アクセス許可" ? "アクセス禁止" : "アクセス許可";
                    })
                    .catch(error => {
                        console.error("エラー:", error);
                        alert("ステータス変更に失敗しました");
                    });
            });
        });
    }

    function closeEditModal() {
        document.getElementById("editUserModal").style.display = "none";
        document.getElementById("modalOverlay").style.display = "none";
    }

    function toggleUserType() {
        const generalFields = document.getElementById("generalFields");
        generalFields.style.display = document.getElementById("generalRadio").checked ? "block" : "none";
    }

    function saveEdit() {
        const form = document.getElementById("editUserForm");
        form.submit(); // HTML form送信
    }

	function showEditModal() {
	    const modal = document.getElementById("editUserModal");
	    const overlay = document.getElementById("modalOverlay");

	    if (!modal || !overlay) {
	        console.error("モーダル要素が見つかりません");
	        return;
	    }

	    modal.style.display = "block";
	    overlay.style.display = "block";
	}

//	編集ボタンを押したらリセットされる
function openEditModal(button) {
    resetEditForm(); // ← 最初に呼ぶ！

    const loginId = button.getAttribute("data-loginid");
    fetch(`/users/detail?loginId=${encodeURIComponent(loginId)}`)
        .then(res => res.json())
        .then(user => {
            document.getElementById("originalLoginId").value = user.loginId;
            document.getElementById("editName").value = user.loginId;
            document.getElementById("editEmail").value = user.mail;
            document.getElementById("editStatus").value = user.access;
            document.getElementById("furigana").value = user.furigana || "";
            document.getElementById("gender").value = user.gender || "1";
            document.getElementById("age").value = user.age || "";
            document.getElementById("introduction").value = user.introduction || "";
            document.getElementById(user.authority === 1 ? "adminRadio" : "generalRadio").checked = true;

            toggleUserType();
            showEditModal();
        });
}

//	編集ボタンを押したらリセットされる
function resetEditForm() {
    // 入力値のリセット
    document.getElementById("editUserForm").reset();

    // ラジオボタン・セレクト・手動入力分のリセット
    document.getElementById("editName").value = "";
    document.getElementById("originalLoginId").value = "";
    document.getElementById("editEmail").value = "";
    document.getElementById("editStatus").value = "1";
    document.getElementById("furigana").value = "";
    document.getElementById("gender").value = "1";
    document.getElementById("age").value = "";
    document.getElementById("introduction").value = "";
    document.getElementById("adminRadio").checked = true;

    // エラーメッセージ要素のテキストをすべて消す
    document.querySelectorAll(".text-danger").forEach(el => {
        el.textContent = "";
    });

    // アラートバナーも消す（あれば）
    const alertBox = document.querySelector(".alert");
    if (alertBox) {
        alertBox.remove();
    }

    toggleUserType(); // 一般用フィールド切り替え
}





    // ✅ バリデーションエラー後など、サーバーからの指示でモーダル表示
	const showModalFlag = document.body.getAttribute("data-show-edit-modal");
	const isReload = performance.navigation.type === 1; // 1 = Reload
	if (showModalFlag === "true" && !isReload) {
	    showEditModal();
	    toggleUserType();
	}

	function openEditModal(button) {
	    resetEditForm(); // ← 既存のリセット処理

	    // ✅ メッセージバナーも消す
	    const alertBox = document.querySelector(".alert");
	    if (alertBox) {
	        alertBox.remove();
	    }

	    const loginId = button.getAttribute("data-loginid");
	    fetch(`/users/detail?loginId=${encodeURIComponent(loginId)}`)
	        .then(res => res.json())
	        .then(user => {
	            document.getElementById("originalLoginId").value = user.loginId;
	            document.getElementById("editName").value = user.loginId;
	            document.getElementById("editEmail").value = user.mail;
	            document.getElementById("editStatus").value = user.access;
	            document.getElementById("furigana").value = user.furigana || "";
	            document.getElementById("gender").value = user.gender || "1";
	            document.getElementById("age").value = user.age || "";
	            document.getElementById("introduction").value = user.introduction || "";
	            document.getElementById(user.authority === 1 ? "adminRadio" : "generalRadio").checked = true;

	            toggleUserType();
	            showEditModal();
	        });
	}


    showPage(currentPage);
    setupPagination();
    addEventListeners();

    // グローバル関数化（外部HTMLからも使えるように）
    window.closeEditModal = closeEditModal;
    window.toggleUserType = toggleUserType;
    window.saveEdit = saveEdit;
	window.openEditModal = openEditModal;

});
