document.addEventListener("DOMContentLoaded", function () {
    //  アカウント復元処理
    function restoreAccount(button) {
        const userId = button.getAttribute("data-id");

        fetch(`/users/${userId}/restore`, { method: "PUT" })
            .then(response => response.text())
            .then(message => {
                alert(message);
                button.closest("tr").remove();
                applyPagination(); // 再描画
            })
            .catch(error => console.error("Error:", error));
    }

    // 完全削除処理
    function permanentlyDeleteAccount(button) {
        let loginId = button.getAttribute("data-id");

        if (!confirm("本当にこのアカウントを完全に削除しますか？")) {
            return;
        }

        fetch(`/users/${loginId}/permanentDelete`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            }
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text); });
            }
            return response.text();
        })
        .then(data => {
            alert(data);
            button.closest("tr").remove();
            applyPagination(); // 再描画
        })
        .catch(error => {
            console.error("削除エラー:", error);
            alert("削除に失敗しました: " + error.message);
        });
    }

    // ✅ グローバルに関数公開
    window.restoreAccount = restoreAccount;
    window.permanentlyDeleteAccount = permanentlyDeleteAccount;

    // ==============================
    // ✅ ページネーション処理
    // ==============================
    const rowsPerPage = 5;
    const tableBody = document.getElementById("userTableBody");
    const pagination = document.getElementById("pagination");

    function applyPagination() {
        const rows = Array.from(tableBody.querySelectorAll("tr"));

        function showPage(page) {
            const start = (page - 1) * rowsPerPage;
            const end = start + rowsPerPage;

            rows.forEach((row, index) => {
                row.style.display = index >= start && index < end ? "" : "none";
            });

            renderPagination(page, rows.length);
        }

        function renderPagination(currentPage, totalItems) {
            const pageCount = Math.ceil(totalItems / rowsPerPage);
            pagination.innerHTML = "";

            for (let i = 1; i <= pageCount; i++) {
                const btn = document.createElement("button");
                btn.innerText = i;
                btn.className = "page-btn";
                if (i === currentPage) btn.classList.add("active");

                btn.addEventListener("click", () => showPage(i));
                pagination.appendChild(btn);
            }
        }

        if (rows.length > 0) {
            showPage(1);
        } else {
            pagination.innerHTML = ""; // 削除後に行が0件になったらボタン非表示
        }
    }

    applyPagination();
});
