package com.bookstore.order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Kafka PRODUCER: tells other services "an order was placed". */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    public static final String TOPIC = "order-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(OrderEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            // send() returns a CompletableFuture (Java 8) - we don't block, we react when it finishes
            kafkaTemplate.send(TOPIC, event.bookId(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Published order {} to {} (offset {})", event.orderId(), TOPIC,
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to publish order {}: {}", event.orderId(), ex.getMessage());
                        }
                    });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not convert event to JSON", e);
        }
    }
}
