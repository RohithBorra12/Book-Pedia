package com.bookstore.book.kafka;

// Same shape as the event order-service publishes (sent as JSON text).
public record OrderEvent(Long orderId, String username, String bookId, int quantity, double totalPrice) {
}
