package com.bookstore.order.config;

import com.bookstore.order.kafka.OrderEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Spring creates this topic on startup if it doesn't exist
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(OrderEventPublisher.TOPIC).partitions(1).replicas(1).build();
    }
}
