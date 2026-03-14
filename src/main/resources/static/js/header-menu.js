document.addEventListener('DOMContentLoaded', function () {
    // Admin menu toggle
    const adminBtn = document.querySelector('.nav-admin .btn-admin');
    const adminMenu = document.querySelector('.nav-admin .admin-menu');
    if (adminBtn && adminMenu) {
        adminBtn.addEventListener('click', function (e) {
            e.preventDefault();
            adminMenu.classList.toggle('open');
            // close user menu if open
            const um = document.querySelector('.user-menu.open'); if (um) um.classList.remove('open');
        });
    }

    // User menu toggle
    const userBtn = document.querySelector('.nav-user .user-btn');
    const userMenu = document.querySelector('.nav-user .user-menu');
    if (userBtn && userMenu) {
        userBtn.addEventListener('click', function (e) {
            e.preventDefault();
            userMenu.classList.toggle('open');
            const am = document.querySelector('.admin-menu.open'); if (am) am.classList.remove('open');
        });
    }

    // Close menus when clicking outside
    document.addEventListener('click', function (e) {
        const insideAdmin = e.target.closest('.nav-admin');
        const insideUser = e.target.closest('.nav-user');
        if (!insideAdmin) {
            const am = document.querySelector('.admin-menu.open'); if (am) am.classList.remove('open');
        }
        if (!insideUser) {
            const um = document.querySelector('.user-menu.open'); if (um) um.classList.remove('open');
        }
    });

    // Close on Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            const am = document.querySelector('.admin-menu.open'); if (am) am.classList.remove('open');
            const um = document.querySelector('.user-menu.open'); if (um) um.classList.remove('open');
        }
    });
});
