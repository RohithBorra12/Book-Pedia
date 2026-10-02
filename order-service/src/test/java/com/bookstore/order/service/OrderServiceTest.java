package com.bookstore.order.service;

import com.bookstore.order.client.BookClient;
import com.bookstore.order.client.BookLookup;
import com.bookstore.order.dto.BookDto;
import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.kafka.OrderEventPublisher;
import com.bookstore.order.model.Order;
import com.bookstore.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository repository;
    @Mock BookClient bookClient;
    @Mock OrderEventPublisher publisher;
    @InjectMocks OrderService orderService;

    private final BookDto book = new BookDto("b1", "Clean Code", "Robert C. Martin", 450.0, 10);

    @BeforeEach
    void setUp() {
        lenient().when(repository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });
    }

    @Test
    void placesOrderAndPublishesEventWhenBookFound() {
        when(bookClient.getBook("b1", "token")).thenReturn(new BookLookup.Found(book));

        OrderResponse response = orderService.placeOrder("alice", new OrderRequest("b1", 2), "token");

        assertEquals("PLACED", response.status());
        assertEquals(900.0, response.totalPrice());
        verify(publisher).publish(any());
    }

    @Test
    void savesPendingOrderWithoutEventWhenBookServiceDown() {
        when(bookClient.getBook("b1", "token")).thenReturn(new BookLookup.Unavailable("ConnectException"));

        OrderResponse response = orderService.placeOrder("alice", new OrderRequest("b1", 1), "token");

        assertEquals("PENDING", response.status());
        verify(publisher, never()).publish(any());
    }

    @Test
    void rejectsOrderWhenNotEnoughStock() {
        when(bookClient.getBook("b1", "token")).thenReturn(new BookLookup.Found(book));

        assertThrows(ResponseStatusException.class,
                () -> orderService.placeOrder("alice", new OrderRequest("b1", 50), "token"));
        verify(repository, never()).save(any());
    }
}
