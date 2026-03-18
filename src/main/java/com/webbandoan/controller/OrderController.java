package com.webbandoan.controller;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.Order;
import com.webbandoan.entity.User;
import com.webbandoan.repository.UserRepository;
import com.webbandoan.service.CartService;
import com.webbandoan.service.OrderService;
import com.webbandoan.service.MomoPaymentService;
import com.webbandoan.service.PaymentMethodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller: Đặt hàng (checkout), trang thành công, và lịch sử đơn hàng.
 * - GET /checkout : form đặt hàng (địa chỉ, SĐT, ghi chú) + tóm tắt giỏ.
 * - POST /checkout : xử lý đặt hàng, redirect /order-success?orderId=...
 * - GET /order-success : trang cảm ơn sau khi đặt hàng.
 * - GET /orders : danh sách đơn hàng của user (lịch sử).
 * - GET /orders/{id} : chi tiết một đơn hàng.
 */
@Slf4j
@Controller
@RequestMapping
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final PaymentMethodService paymentMethodService;
    private final MomoPaymentService momoPaymentService;

    public OrderController(OrderService orderService, CartService cartService, UserRepository userRepository, 
                          PaymentMethodService paymentMethodService, MomoPaymentService momoPaymentService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.paymentMethodService = paymentMethodService;
        this.momoPaymentService = momoPaymentService;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping("/checkout")
    public String checkoutPage(CsrfToken csrfToken, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartService.getCartItems(user);
        BigDecimal totalAmount = cartService.getTotalAmount(user);

        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", user);
        
        // Thêm danh sách phương thức thanh toán (thêm try-catch để tránh 500)
        try {
            var paymentMethods = paymentMethodService.getAllActive();
            model.addAttribute("paymentMethods", paymentMethods != null ? paymentMethods : List.of());
        } catch (Exception e) {
            model.addAttribute("paymentMethods", List.of());
        }
        
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @RequestParam String shippingAddress,
            @RequestParam String phone,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) Long paymentMethodId,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập địa chỉ giao hàng.");
            return "redirect:/checkout";
        }
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập số điện thoại.");
            return "redirect:/checkout";
        }

        Order order = orderService.placeOrder(user, shippingAddress.trim(), phone.trim(), note, paymentMethodId);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống. Không thể đặt hàng.");
            return "redirect:/cart";
        }

        log.info("Order created: id={}, paymentMethodId={}", order.getId(), paymentMethodId);

        boolean isMomo = order.getPaymentMethod() != null && "MOMO".equalsIgnoreCase(order.getPaymentMethod().getCode());
        if (isMomo) {
            try {
                var momoResponse = momoPaymentService.createPaymentRequest(order);
                if (momoResponse.isSuccess() && momoResponse.getPayUrl() != null && !momoResponse.getPayUrl().isBlank()) {
                    return "redirect:" + momoResponse.getPayUrl();
                }

                String message = momoResponse.getMessage() != null && !momoResponse.getMessage().isBlank()
                        ? momoResponse.getMessage()
                        : "MoMo chưa trả về đường dẫn thanh toán.";
                orderService.cancelMomoOrderAndRestoreCart(order.getId());
                redirectAttributes.addFlashAttribute("errorMessage", message);
                return "redirect:/checkout";
            } catch (Exception ex) {
                log.error("Failed to create MoMo payment for orderId={}", order.getId(), ex);
                String detailMessage = ex.getMessage();
                if (ex.getCause() != null && ex.getCause().getMessage() != null && !ex.getCause().getMessage().isBlank()) {
                    detailMessage = ex.getCause().getMessage();
                }
                if (detailMessage == null || detailMessage.isBlank()) {
                    detailMessage = "Không thể tạo link thanh toán MoMo. Đơn hàng đã được lưu và đang chờ xử lý.";
                } else {
                    detailMessage = "Không thể tạo link thanh toán MoMo: " + detailMessage;
                }
                orderService.cancelMomoOrderAndRestoreCart(order.getId());
                redirectAttributes.addFlashAttribute("errorMessage", detailMessage);
                return "redirect:/checkout";
            }
        }

        // COD hoặc phương thức khác: redirect về trang thành công
        redirectAttributes.addAttribute("orderId", order.getId());
        return "redirect:/order-success";
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam Long orderId, CsrfToken csrfToken, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Order order = orderService.findByIdAndUser(orderId, user);
        if (order == null) {
            return "redirect:/";
        }
        model.addAttribute("order", order);
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "order-success";
    }

    @GetMapping("/orders")
    public String orderHistory(CsrfToken csrfToken, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<Order> orders = orderService.findByUser(user);
        model.addAttribute("orders", orders);
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "order-history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, CsrfToken csrfToken, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Order order = orderService.findByIdAndUser(id, user);
        if (order == null) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "order-detail";
    }
}
