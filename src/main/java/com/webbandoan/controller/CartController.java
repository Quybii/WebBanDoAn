package com.webbandoan.controller;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.User;
import com.webbandoan.repository.UserRepository;
import com.webbandoan.service.CartService;
import com.webbandoan.service.ShopService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller: Giỏ hàng.
 * - GET /cart : xem giỏ hàng.
 * - GET /cart/add/{foodId} : thêm món vào giỏ (redirect về /cart hoặc về trang món).
 * - POST /cart/update : cập nhật số lượng.
 * - POST /cart/remove/{id} : xóa dòng khỏi giỏ.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final ShopService shopService;

    public CartController(CartService cartService, UserRepository userRepository, ShopService shopService) {
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.shopService = shopService;
    }

    private record AddToCartRequest(Long foodId, Integer quantity, Long parentCartItemId) {
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping("/debug/user")
    public ResponseEntity<?> debugUser() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "message", "Chưa đăng nhập"
            ));
        }
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "username", user.getUsername(),
            "userId", user.getId(),
            "message", "Đã đăng nhập"
        ));
    }

    @GetMapping
    public String viewCart(CsrfToken csrfToken, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<CartItem> items = cartService.getCartItems(user);
        BigDecimal total = cartService.getTotalAmount(user);
        model.addAttribute("cartItems", items);
        model.addAttribute("totalAmount", total);
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "cart";
    }

    @GetMapping("/add/{foodId}")
    public String addToCart(
            @PathVariable Long foodId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String redirectTo,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (!shopService.isOpen()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hiện tại cửa hàng đóng cửa, không nhận đơn.");
            return "redirect:/foods/" + foodId;
        }
        CartItem createdItem = cartService.addItem(user, foodId, quantity);
        if (createdItem == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm món vào giỏ.");
            return "redirect:/foods/" + foodId;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm món vào giỏ.");
        if ("cart".equals(redirectTo)) {
            return "redirect:/cart";
        }
        return "redirect:/foods/" + foodId;
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> addToCartForm(
            @RequestParam Long foodId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Long parentCartItemId) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập",
                    "debug", "User not authenticated"
                ));
            }
            
            if (!shopService.isOpen()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Hiện tại cửa hàng đóng cửa, không nhận đơn.",
                    "debug", "Shop is closed"
                ));
            }

            if (foodId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "foodId không hợp lệ",
                    "debug", "foodId is null"
                ));
            }

            CartItem createdItem = cartService.addItem(user, foodId, quantity, parentCartItemId);
            if (createdItem == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể thêm vào giỏ"
                ));
            }
            int count = cartService.getCartItems(user).size();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cartItemId", createdItem != null ? createdItem.getId() : null,
                "cartCount", count,
                "message", "Đã thêm vào giỏ.",
                "debug", "Added foodId=" + foodId + ", qty=" + quantity + " for user=" + user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi thêm vào giỏ",
                "error", e.getMessage(),
                "exceptionType", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addToCartJson(@RequestBody(required = false) AddToCartRequest req) {
        try {
            if (req == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Request body không hợp lệ",
                    "debug", "Request is null - check Content-Type: application/json"
                ));
            }
            
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập",
                    "debug", "User not authenticated"
                ));
            }
            
            if (!shopService.isOpen()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Hiện tại cửa hàng đóng cửa, không nhận đơn.",
                    "debug", "Shop is closed"
                ));
            }

            if (req.foodId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "foodId không hợp lệ",
                    "debug", "foodId is null in request"
                ));
            }

            int quantity = (req.quantity() == null || req.quantity() <= 0) ? 1 : req.quantity();
            CartItem createdItem = cartService.addItem(user, req.foodId(), quantity, req.parentCartItemId());
            if (createdItem == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể thêm vào giỏ"
                ));
            }
            int count = cartService.getCartItems(user).size();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cartItemId", createdItem != null ? createdItem.getId() : null,
                "cartCount", count,
                "message", "Đã thêm vào giỏ.",
                "debug", "Added foodId=" + req.foodId() + ", qty=" + quantity + " for user=" + user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi thêm vào giỏ",
                "error", e.getMessage(),
                "exceptionType", e.getClass().getSimpleName(),
                "debug", "Request body: " + (req != null ? "foodId=" + req.foodId() + ", qty=" + req.quantity() : "null")
            ));
        }
    }

    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam Long cartItemId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        cartService.updateQuantity(user, cartItemId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/remove/{id}")
    public String removeItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (cartService.removeItem(user, id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món khỏi giỏ.");
        }
        return "redirect:/cart";
    }

    // ===== API Endpoints for AJAX =====
    
    @PostMapping(value = "/api/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> apiUpdateQuantity(@RequestBody Map<String, Object> request) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập"
                ));
            }

            Long cartItemId = Long.parseLong(request.get("cartItemId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());

            if (quantity < 1 || quantity > 99) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Số lượng phải từ 1 đến 99"
                ));
            }

            cartService.updateQuantity(user, cartItemId, quantity);
            
            // Trả về thông tin giỏ hàng cập nhật
            List<CartItem> items = cartService.getCartItems(user);
            BigDecimal total = cartService.getTotalAmount(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật thành công",
                "cartCount", items.size(),
                "totalAmount", total,
                "items", items.stream().map(item -> Map.of(
                    "id", item.getId(),
                    "foodId", item.getFood().getId(),
                    "foodName", item.getFood().getName(),
                    "quantity", item.getQuantity(),
                    "price", item.getFood().getPrice(),
                    "subtotal", item.getSubtotal()
                )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi cập nhật",
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping(value = "/api/remove/{id}")
    public ResponseEntity<?> apiRemoveItem(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập"
                ));
            }

            boolean removed = cartService.removeItem(user, id);
            
            if (!removed) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy mục trong giỏ"
                ));
            }

            // Trả về thông tin giỏ hàng cập nhật
            List<CartItem> items = cartService.getCartItems(user);
            BigDecimal total = cartService.getTotalAmount(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa khỏi giỏ",
                "cartCount", items.size(),
                "totalAmount", total,
                "items", items.stream().map(item -> Map.of(
                    "id", item.getId(),
                    "foodId", item.getFood().getId(),
                    "foodName", item.getFood().getName(),
                    "quantity", item.getQuantity(),
                    "price", item.getFood().getPrice(),
                    "subtotal", item.getSubtotal()
                )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi xóa",
                "error", e.getMessage()
            ));
        }
    }
}
