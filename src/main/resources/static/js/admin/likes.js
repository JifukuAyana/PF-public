function likeUser(button) {
    const loginId = button.getAttribute("data-loginid");
    const path = window.location.pathname;

    // どちらもOKにする
    const isPublic = path.startsWith("/publicUsers");

    const url = isPublic ? "/public/like" : "/like";

    fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `loginId=${encodeURIComponent(loginId)}`
    })
    .then(response => {
        if (!response.ok) throw new Error("通信エラー");
        return response.text();
    })
    .then(updatedLikes => {
        const likeDisplay = document.getElementById("likes-" + loginId);
        if (likeDisplay) {
            likeDisplay.textContent = updatedLikes;
        }
    })
    .catch(() => alert("いいねに失敗しました"));
}
