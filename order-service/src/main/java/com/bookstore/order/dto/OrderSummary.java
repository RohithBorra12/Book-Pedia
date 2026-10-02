package com.bookstore.order.dto;

public record OrderSummary(int orderCount, double totalSpent, OrderResponse latestOrder) {
}
