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

    /**
     * Thêm món vào giỏ (hoặc tăng số lượng nếu đã có).
     */
    @Transactional
    public void addItem(User user, Long foodId, int quantity) {
        if (quantity <= 0 || user == null || foodId == null) return;
        
        Food food = foodRepository.findById(foodId).orElse(null);
        if (food == null || !food.getIsAvailable()) return;

        CartItem item = cartItemRepository.findByUserAndFood(user, food).orElse(null);
        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            item = new CartItem(user, food, quantity);
            cartItemRepository.save(item);
        }
    }

    /**
     * Xóa một dòng khỏi giỏ (chỉ khi thuộc user hiện tại).
     */
    @Transactional
    public boolean removeItem(User user, Long cartItemId) {
        if (cartItemId == null || user == null) return false;
        
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !item.getUser().getId().equals(user.getId())) return false;
        
        cartItemRepository.delete(item);
        return true;
    }

    /**
     * Cập nhật số lượng. Nếu quantity <= 0 thì xóa dòng đó.
     */
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

    /**
     * Lấy danh sách dòng trong giỏ của user (có load Food).
     */
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        if (user == null) return List.of();
        return cartItemRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Tính tổng tiền giỏ hàng (số lượng * đơn giá từng món).
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmount(User user) {
        if (user == null) return BigDecimal.ZERO;
        
        List<CartItem> items = cartItemRepository.findByUserOrderByCreatedAtDesc(user);
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Xóa toàn bộ giỏ hàng của user (sau khi đặt hàng thành công).
     */
    @Transactional
    public void clearCart(User user) {
        if (user != null) {
            cartItemRepository.deleteByUser(user);
        }
    }
}
