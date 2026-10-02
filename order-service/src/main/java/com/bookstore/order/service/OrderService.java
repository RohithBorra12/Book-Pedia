package com.bookstore.order.service;

import com.bookstore.order.client.BookClient;
import com.bookstore.order.client.BookLookup;
import com.bookstore.order.dto.BookDto;
import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderSummary;
import com.bookstore.order.kafka.OrderEvent;
import com.bookstore.order.kafka.OrderEventPublisher;
import com.bookstore.order.model.Order;
import com.bookstore.order.model.OrderStatus;
import com.bookstore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final BookClient bookClient;
    private final OrderEventPublisher publisher;

    public OrderResponse placeOrder(String username, OrderRequest request, String authHeader) {
        BookLookup lookup = bookClient.getBook(request.bookId(), authHeader);

        var order = new Order(); // "var" local type inference (Java 10)
        order.setUsername(username);
        order.setBookId(request.bookId());
        order.setQuantity(request.quantity());

        // Pattern matching for switch + record patterns + "when" guards (Java 21)
        switch (lookup) {
            case BookLookup.Found(BookDto book) when book.stock() < request.quantity() ->
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Only " + book.stock() + " copies of '" + book.title() + "' left");
            case BookLookup.Found(BookDto book) -> {
                order.setBookTitle(book.title());
                order.setTotalPrice(book.price() * request.quantity());
                order.setStatus(OrderStatus.PLACED);
            }
            case BookLookup.Unavailable(String reason) -> {
                log.warn("Saving order as PENDING because book-service is unavailable ({})", reason);
                order.setBookTitle("Unknown (book service unavailable)");
                order.setStatus(OrderStatus.PENDING);
            }
        }

        Order saved = repository.save(order);
        log.info("Order {} saved for {} with status {}", saved.getId(), username, saved.getStatus());

        if (saved.getStatus() == OrderStatus.PLACED) {
            publisher.publish(new OrderEvent(saved.getId(), username, saved.getBookId(),
                    saved.getQuantity(), saved.getTotalPrice()));
        }
        return OrderResponse.from(saved);
    }

    public List<OrderResponse> myOrders(String username) {
        return repository.findByUsernameOrderByCreatedAtDesc(username).stream().map(OrderResponse::from).toList();
    }

    public List<OrderResponse> allOrders() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(OrderResponse::from).toList();
    }

    public OrderSummary summary(String username) {
        List<OrderResponse> orders = myOrders(username);
        double totalSpent = orders.stream()
                .filter(o -> OrderStatus.PLACED.name().equals(o.status()))
                .mapToDouble(OrderResponse::totalPrice)
                .sum();
        // getFirst() - Sequenced Collections (Java 21)
        OrderResponse latest = orders.isEmpty() ? null : orders.getFirst();
        return new OrderSummary(orders.size(), totalSpent, latest);
    }
}
