function showDetailModal(button) {
    document.getElementById('modalImage').src = button.getAttribute('data-image');
    document.getElementById('modalName').textContent = button.getAttribute('data-name');
	
	// ✅❤️ ここで一覧画面の like 数を取得してモーダルに反映
	   const loginId = button.getAttribute('data-name');
	   const likes = document.getElementById(`likes-${loginId}`)?.textContent || "0";
	   document.getElementById('modalLikeCount').textContent = likes;

    // ふりがな
    const furigana = button.getAttribute('data-furigana');
    document.getElementById('modalFurigana').textContent = furigana && furigana.trim() !== "" ? furigana : "-";

    // 性別（1: 男性, 2: 女性）
    const genderValue = button.getAttribute('data-gender');
    let genderText = "";
    switch (genderValue) {
        case "1":
            genderText = "男性";
            break;
        case "2":
            genderText = "女性";
            break;
        default:
            genderText = "その他";
    }
    document.getElementById('modalGender').textContent = genderText;

    // 年齢
    const age = button.getAttribute('data-age');
    document.getElementById('modalAge').textContent = age && age.trim() !== "" && age !== "null" ? age + "歳" : "-";

	   // 自己紹介（未入力なら非表示）
	    const intro = button.getAttribute('data-intro');
	    const introDiv = document.getElementById('modalIntro');
	    if (intro && intro.trim() !== "") {
	        introDiv.textContent = intro;
	        introDiv.style.display = "block";
	    } else {
	        introDiv.style.display = "none";
	    }

	    // モーダル表示
	    document.getElementById('detailModal').style.display = 'flex';
	}

	
//	詳細画面のいいね
	function likeFromModal() {
	    const loginId = document.getElementById('modalName').textContent;

	    fetch('/public/like', {
	        method: 'POST',
	        headers: {
	            'Content-Type': 'application/x-www-form-urlencoded'
	        },
	        body: `loginId=${encodeURIComponent(loginId)}`
	    })
	    .then(res => res.text())
	    .then(newLikes => {
	        // モーダル内の like 数更新
	        document.getElementById('modalLikeCount').textContent = newLikes;

	        // 一覧側も同時に更新
	        const likeSpan = document.getElementById(`likes-${loginId}`);
	        if (likeSpan) {
	            likeSpan.textContent = newLikes;
	        }
	    })
	    .catch(err => console.error('いいねに失敗しました:', err));
	}

	

	function closeAskPopup() {
	    // モーダルを非表示にする処理（必要に応じて残す）
	    document.getElementById('askModal').style.display = 'none';

	    // URLのパス部分を保持（例: /publicUsers か /publicUsers/monthly）
	    const baseUrl = window.location.pathname;

	    // 末尾に「?ask=success」が付いていれば除去してリダイレクト
	    if (window.location.search.includes("ask=success")) {
	        window.location.href = baseUrl;
	    }
	}

	
	function closeDetailModal() {
		    document.getElementById('detailModal').style.display = 'none';
		}