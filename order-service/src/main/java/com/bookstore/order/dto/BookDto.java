package com.bookstore.order.dto;

// What book-service returns from GET /api/books/{id}
public record BookDto(String id, String title, String author, double price, int stock) {
}
