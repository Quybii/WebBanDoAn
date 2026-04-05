package com.webbandoan.service;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.Food;
import com.webbandoan.entity.User;
import com.webbandoan.repository.CartItemRepository;
import com.webbandoan.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service: Giỏ hàng.
 * Thêm món, xóa món, cập nhật số lượng, lấy danh sách giỏ, tính tổng tiền.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;

    @Transactional
    public CartItem addItem(User user, Long foodId, int quantity) {
        return addItem(user, foodId, quantity, null);
    }

    @Transactional
    public CartItem addItem(User user, Long foodId, int quantity, Long parentCartItemId) {
        if (quantity <= 0 || user == null || foodId == null) return null;
        
        Food food = foodRepository.findById(foodId).orElse(null);
        if (food == null || !Boolean.TRUE.equals(food.getIsAvailable())) return null;

        CartItem parentCartItem = null;
        if (parentCartItemId != null) {
            parentCartItem = cartItemRepository.findById(parentCartItemId).orElse(null);
            if (parentCartItem != null && !parentCartItem.getUser().getId().equals(user.getId())) {
                parentCartItem = null;
            }
        }

        CartItem item = new CartItem(user, food, quantity, parentCartItem);
        return cartItemRepository.save(item);
    }

    @Transactional
    public boolean removeItem(User user, Long cartItemId) {
        if (cartItemId == null || user == null) return false;
        
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !item.getUser().getId().equals(user.getId())) return false;
        
        cartItemRepository.deleteByParentCartItem(item);
        cartItemRepository.delete(item);
        return true;
    }


    @Transactional
    public boolean updateQuantity(User user, Long cartItemId, int quantity) {
        if (cartItemId == null || user == null) return false;
        
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !item.getUser().getId().equals(user.getId())) return false;
        
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return true;
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return true;
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        if (user == null) return List.of();
        return cartItemRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAmount(User user) {
        if (user == null) return BigDecimal.ZERO;
        
        List<CartItem> items = cartItemRepository.findByUserOrderByCreatedAtDesc(user);
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void clearCart(User user) {
        if (user != null) {
            cartItemRepository.deleteByUser(user);
        }
    }
}
