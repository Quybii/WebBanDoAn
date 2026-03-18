package com.webbandoan.service;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.Order;
import com.webbandoan.entity.OrderDetail;
import com.webbandoan.entity.PaymentMethod;
import com.webbandoan.entity.User;
import com.webbandoan.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service: Đơn hàng.
 * Đặt hàng: tạo Order + OrderDetail từ giỏ, xóa giỏ. Toàn bộ trong một transaction.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final PaymentMethodService paymentMethodService;

    public OrderService(OrderRepository orderRepository, CartService cartService, PaymentMethodService paymentMethodService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Đặt hàng: lấy giỏ của user → tạo Order + OrderDetail → lưu Order → xóa giỏ.
     * Dùng @Transactional để đảm bảo: nếu lỗi giữa chừng thì rollback (không mất giỏ, không tạo đơn dở).
     *
     * @param user Người dùng đặt hàng
     * @param shippingAddress Địa chỉ giao hàng
     * @param phone SĐT nhận hàng
     * @param note Ghi chú đơn hàng
     * @param paymentMethodId ID phương thức thanh toán (1=COD, 2=MOMO)
     * @return Order đã lưu, hoặc null nếu giỏ trống
     */
    @Transactional
    public Order placeOrder(User user, String shippingAddress, String phone, String note, Long paymentMethodId) {
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems == null || cartItems.isEmpty()) {
            return null;
        }

        BigDecimal totalAmount = cartService.getTotalAmount(user);
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress != null ? shippingAddress.trim() : "");
        order.setPhone(phone != null ? phone.trim() : "");
        order.setNote(note != null ? note.trim() : null);

        // Set phương thức thanh toán (mặc định COD nếu không chỉ định)
        PaymentMethod paymentMethod = null;
        if (paymentMethodId != null) {
            paymentMethod = paymentMethodService.getById(paymentMethodId);
        }
        if (paymentMethod == null) {
            paymentMethod = paymentMethodService.getCOD();  // Mặc định COD
        }
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");

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
     * Overload placeOrder không có paymentMethodId (dùng cho backward compatibility)
     */
    @Transactional
    public Order placeOrder(User user, String shippingAddress, String phone, String note) {
        return placeOrder(user, shippingAddress, phone, note, null);
    }

    /**
     * Lấy đơn hàng theo id (chỉ khi thuộc user).
     */
    @Transactional(readOnly = true)
    public Order findByIdAndUser(Long orderId, User user) {
        if (orderId == null || user == null) return null;
        
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
        if (user == null) return List.of();
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
        if (orderId == null) return null;
        return orderRepository.findById(orderId).orElse(null);
    }


    /**
     * Cập nhật trạng thái đơn hàng (admin dùng).
     */
    @Transactional
    public Order updateStatus(Long orderId, String status) {
        if (orderId == null) return null;
        
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /**
     * Cập nhật trạng thái thanh toán của đơn hàng (dùng khi callback từ MoMo)
     */
    @Transactional
    public Order updateOrderPaymentStatus(Order order) {
        if (order == null) return null;
        return orderRepository.save(order);
    }

    /**
     * Cập nhật kết quả thanh toán MoMo cho đơn hàng.
     */
    @Transactional
    public Order updatePaymentResult(Long orderId, String transactionId, String paymentStatus) {
        if (orderId == null) {
            return null;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return null;
        }

        if (transactionId != null && !transactionId.trim().isEmpty()) {
            order.setTransactionId(transactionId.trim());
        }
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            order.setPaymentStatus(paymentStatus.trim());
        }
        return orderRepository.save(order);
    }

    /**
     * Hủy đơn hàng thanh toán MoMo thất bại và khôi phục lại giỏ hàng cho user.
     * Dùng khi MoMo bị từ chối, người dùng hủy giao dịch, hoặc không tạo được link thanh toán.
     */
    @Transactional
    public Order cancelMomoOrderAndRestoreCart(Long orderId) {
        if (orderId == null) {
            return null;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return null;
        }

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            return order;
        }

        order.setStatus("CANCELLED");
        order.setPaymentStatus("FAILED");
        orderRepository.save(order);

        if (order.getUser() != null && order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            cartService.clearCart(order.getUser());
            order.getOrderDetails().forEach(detail -> {
                if (detail.getFood() != null && detail.getQuantity() != null && detail.getQuantity() > 0) {
                    cartService.addItem(order.getUser(), detail.getFood().getId(), detail.getQuantity());
                }
            });
        }

        return order;
    }
}
