//お問い合わせのポップアップ

function openAskPopup() {
	    document.getElementById("askModal").style.display = "block";
	}
	function closeAskPopup() {
	    document.getElementById("askModal").style.display = "none";
	}
	window.onclick = function(event) {
	    if (event.target.classList.contains("modal")) {
	        closeAskPopup();
	    }
	}
	
	
	// URLパラメータで送信成功時にポップアップ表示（?ask=success）
	document.addEventListener("DOMContentLoaded", () => {
	  const urlParams = new URLSearchParams(window.location.search);
	  if (urlParams.get("ask") === "success") {
	    openAskPopup();
	    const msgBox = document.getElementById("askMessage");
	    if (msgBox) {
	      msgBox.style.display = "block";
	      msgBox.textContent = "お問い合わせ内容をメールで送信しました！";
	    }
	  }
	});