/* cart.js - handles AJAX add-to-cart, buy-now and header count update */

document.addEventListener('DOMContentLoaded', function () {
    // Lắng nghe sự kiện click trên toàn body
    document.body.addEventListener('click', function (e) {
        
        // 1. Nếu click vào nút "Thêm vào giỏ"
        const btnAdd = e.target.closest('.btn-add-cart-ajax');
        if (btnAdd) {
            e.preventDefault();
            const foodId = btnAdd.getAttribute('data-food-id');
            const qty = getQuantityForButton(btnAdd);
            addToCartAjax(foodId, qty, btnAdd, false); // false = không chuyển hướng
            return;
        }

        // 2. Nếu click vào nút "Mua ngay"
        const btnBuy = e.target.closest('.btn-buy-now-ajax');
        if (btnBuy) {
            e.preventDefault();
            const foodId = btnBuy.getAttribute('data-food-id');
            const qty = getQuantityForButton(btnBuy);
            addToCartAjax(foodId, qty, btnBuy, true); // true = chuyển hướng sang /cart
            return;
        }
    });
});

// Hàm dùng chung để lấy số lượng từ ô input (trang chi tiết) hoặc từ data (trang danh sách)
function getQuantityForButton(btn) {
    const qtyInput = document.getElementById('food-quantity');
    if (qtyInput && (btn.id === 'btn-add-cart-detail' || btn.id === 'btn-buy-now-detail')) {
        return qtyInput.value;
    }
    return btn.getAttribute('data-quantity') || 1;
}

// Hàm gọi API
function addToCartAjax(foodId, quantity, btn, isBuyNow) {
    if (!window._csrf || !window._csrf.token) {
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
            
            // LOGIC QUAN TRỌNG NHẤT Ở ĐÂY:
            if (isBuyNow) {
                // Nếu là Mua ngay -> Bay thẳng sang trang giỏ hàng
                window.location.href = '/cart'; 
            } else {
                // Nếu là Thêm vào giỏ -> Chỉ cập nhật số trên góc và hiện thông báo
                const cnt = document.getElementById('header-cart-count');
                if (cnt) cnt.textContent = data.cartCount;
                showToast(data.message || 'Đã thêm vào giỏ.', 'success');
            }

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
