package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.PagedResponse;
import com.ecommerce.dto.PlaceOrderRequest;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Order management endpoints.
 * All require authentication.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders
     * Converts the current user's cart into a placed order.
     * Stock is deducted and the cart is cleared.
     */

    //Older function
    /**@PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder() {
    return ResponseEntity.ok(ApiResponse.success(orderService.placeOrder()));
    }
     */
    // Updated one
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.placeOrder(req), "Order placed successfully"));
    }


    /**
     * GET /api/orders
     * Paginated order history for the current user, newest first.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderDto>>> history(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderHistory(page, size)));
    }

    /**
     * GET /api/orders/{id}
     * Full order details including line items.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    /**
     * PUT /api/orders/{id}/cancel
     * Cancel a PENDING order. The stock is NOT automatically restored
     * in this implementation (add that to OrderService if required).
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(id)));
    }
}