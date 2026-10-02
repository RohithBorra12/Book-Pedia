package com.bookstore.order.model;

public enum OrderStatus {
    PLACED,
    PENDING,
    CANCELLED;

    /** Switch expression (Java 14+): returns a value, no "break" needed. */
    public String description() {
        return switch (this) {
            case PLACED -> "Order confirmed";
            case PENDING -> "Saved, but book service was down - will be confirmed later";
            case CANCELLED -> "Order cancelled";
        };
    }
}
