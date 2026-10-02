package com.bookstore.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Java record (Java 16+): immutable data class with constructor, getters, equals, hashCode, toString.
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password) {
}
