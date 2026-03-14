/* cart.js - handles AJAX add-to-cart and header count update */

document.addEventListener('DOMContentLoaded', function () {
    // Delegate click for add-to-cart buttons
    document.body.addEventListener('click', function (e) {
        const btn = e.target.closest('.btn-add-cart-ajax');
        if (!btn) return;
        e.preventDefault();
        const foodId = btn.getAttribute('data-food-id');
        const qty = btn.getAttribute('data-quantity') || 1;
        addToCartAjax(foodId, qty, btn);
    });
});

function addToCartAjax(foodId, quantity, btn) {
    if (!window._csrf || !window._csrf.token) {
        // fallback: navigate to add URL
        window.location.href = '/cart/add/' + foodId + '?redirectTo=cart';
        return;
    }

    btn.classList.add('loading');

    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            [window._csrf.headerName]: window._csrf.token
        },
        body: new URLSearchParams({foodId: foodId, quantity: quantity})
    })
        .then(res => res.json())
        .then(data => {
            btn.classList.remove('loading');
            if (data && data.success) {
                // update header count
                const cnt = document.getElementById('header-cart-count');
                if (cnt) cnt.textContent = data.cartCount;
                showToast(data.message || 'Đã thêm vào giỏ.', 'success');
            } else if (data && data.message) {
                showToast(data.message, 'error');
            } else {
                showToast('Không thể thêm vào giỏ.', 'error');
            }
        })
        .catch(err => {
            btn.classList.remove('loading');
            showToast('Lỗi mạng, thử lại.', 'error');
            console.error(err);
        });
}

function showToast(message, type) {
    // small transient toast in top-right
    const toast = document.createElement('div');
    toast.className = 'site-toast ' + (type === 'success' ? 'success' : 'error');
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.classList.add('visible'), 10);
    setTimeout(() => toast.classList.remove('visible'), 3500);
    setTimeout(() => toast.remove(), 3900);
}
