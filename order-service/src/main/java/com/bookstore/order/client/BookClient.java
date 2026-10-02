package com.bookstore.order.client;

import com.bookstore.order.dto.BookDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Synchronous REST call from order-service to book-service, protected by a CIRCUIT BREAKER.
 *
 * - book-service healthy -> normal call.
 * - book-service down / slow -> fallback() runs instead of crashing.
 * - after several failures the circuit OPENS: calls go straight to fallback for 10s
 *   (no time wasted on a dead service), then it tries again (HALF_OPEN).
 */
@Component
@Slf4j
public class BookClient {

    private final RestClient restClient;

    public BookClient(@Value("${book-service.url}") String bookServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(bookServiceUrl).build();
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallback")
    public BookLookup getBook(String bookId, String authHeader) {
        BookDto book = restClient.get()
                .uri("/api/books/{id}", bookId)
                .header(HttpHeaders.AUTHORIZATION, authHeader) // forward the user's JWT
                .retrieve()
                .body(BookDto.class);
        return new BookLookup.Found(book);
    }

    private BookLookup fallback(String bookId, String authHeader, Throwable ex) {
        // Pattern matching for instanceof (Java 16): no manual cast needed
        if (ex instanceof HttpClientErrorException clientError) {
            // 4xx (e.g. 404 book not found) is the caller's mistake, not an outage
            throw new ResponseStatusException(clientError.getStatusCode(), "Book not found: " + bookId);
        }
        log.warn("Circuit breaker fallback for book {}: {}", bookId, ex.toString());
        return new BookLookup.Unavailable(ex.getClass().getSimpleName());
    }
}
