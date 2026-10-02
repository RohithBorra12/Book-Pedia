package com.bookstore.book.kafka;

import com.bookstore.book.service.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka CONSUMER: order-service publishes "order-events", we reduce the stock.
 * The two services never call each other for this - that's event-driven design.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "book-service")
    public void onOrderPlaced(String message) throws JsonProcessingException {
        OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
        log.info("Received order event: {}", event);
        bookService.reduceStock(event.bookId(), event.quantity());
    }
}
