// サイドバーを開閉する関数
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const rankingContainer = document.getElementById('ranking-container');
    
    if (sidebar.classList.contains('collapsed')) {
        sidebar.classList.remove('collapsed');
        rankingContainer.classList.remove('full-width');
    } else {
        sidebar.classList.add('collapsed');
        rankingContainer.classList.add('full-width');
    }
}

