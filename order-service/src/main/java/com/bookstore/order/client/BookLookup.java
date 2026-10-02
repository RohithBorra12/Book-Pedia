package com.bookstore.order.client;

import com.bookstore.order.dto.BookDto;

/**
 * Sealed interface (Java 17): ONLY these two records can implement it.
 * The compiler knows every possible case, so a switch over it needs no "default".
 */
public sealed interface BookLookup {

    record Found(BookDto book) implements BookLookup {
    }

    record Unavailable(String reason) implements BookLookup {
    }
}
