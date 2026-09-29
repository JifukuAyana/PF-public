function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    const body = document.body;

    sidebar.classList.toggle("collapsed");
    body.classList.toggle("sidebar-open");
    body.classList.toggle("sidebar-closed");
}
