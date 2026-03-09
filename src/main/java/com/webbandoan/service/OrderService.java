package com.webbandoan.service;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.Order;
import com.webbandoan.entity.OrderDetail;
import com.webbandoan.entity.User;
import com.webbandoan.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service: Đơn hàng.
 * Đặt hàng: tạo Order + OrderDetail từ giỏ, xóa giỏ. Toàn bộ trong một transaction.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    /**
     * Đặt hàng: lấy giỏ của user → tạo Order + OrderDetail → lưu Order → xóa giỏ.
     * Dùng @Transactional để đảm bảo: nếu lỗi giữa chừng thì rollback (không mất giỏ, không tạo đơn dở).
     *
     * @return Order đã lưu, hoặc null nếu giỏ trống
     */
    @Transactional
    public Order placeOrder(User user, String shippingAddress, String phone, String note) {
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems == null || cartItems.isEmpty()) {
            return null;
        }

        BigDecimal totalAmount = cartService.getTotalAmount(user);
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress != null ? shippingAddress.trim() : "");
        order.setPhone(phone != null ? phone.trim() : "");
        order.setNote(note != null ? note.trim() : null);

        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail(
                    order,
                    item.getFood(),
                    item.getQuantity(),
                    item.getFood().getPrice()
            );
            order.getOrderDetails().add(detail);
        }

        orderRepository.save(order);
        cartService.clearCart(user);
        return order;
    }

    /**
     * Lấy đơn hàng theo id (chỉ khi thuộc user).
     */
    @Transactional(readOnly = true)
    public Order findByIdAndUser(Long orderId, User user) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            return null;
        }
        return order;
    }

    /**
     * Lấy danh sách đơn hàng của user (Bước 9 dùng).
     */
    @Transactional(readOnly = true)
    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    /**
     * Lấy tất cả đơn hàng (admin dùng - Bước 10).
     */
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * Lấy đơn hàng theo id (admin dùng - không kiểm tra user).
     */
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * Cập nhật trạng thái đơn hàng (admin dùng).
     */
    @Transactional
    public Order updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
