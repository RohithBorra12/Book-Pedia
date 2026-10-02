package com.bookstore.order.dto;

import com.bookstore.order.model.Order;

import java.time.LocalDateTime;

public record OrderResponse(Long id, String username, String bookId, String bookTitle, int quantity,
                            double totalPrice, String status, String statusDescription, LocalDateTime createdAt) {

    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getUsername(), o.getBookId(), o.getBookTitle(), o.getQuantity(),
                o.getTotalPrice(), o.getStatus().name(), o.getStatus().description(), o.getCreatedAt());
    }
}
