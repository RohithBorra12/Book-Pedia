package com.bookstore.order.kafka;

public record OrderEvent(Long orderId, String username, String bookId, int quantity, double totalPrice) {
}
