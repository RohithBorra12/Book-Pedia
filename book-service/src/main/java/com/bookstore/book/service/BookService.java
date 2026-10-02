package com.bookstore.book.service;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.model.Book;
import com.bookstore.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository repository;

    /**
     * First call reads MongoDB and stores the result in Redis under "books".
     * Next calls come straight from Redis (watch the log: the message only appears on a cache miss).
     */
    @Cacheable("books")
    public List<Book> getAll() {
        log.info("Cache MISS - loading books from MongoDB");
        return repository.findAll();
    }

    public Book getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
    }

    public List<Book> searchByAuthor(String author) {
        return repository.findByAuthorContainingIgnoreCase(author);
    }

    /** Streams + Collectors.groupingBy (Java 8): { "author" -> ["title1", "title2"] } */
    public Map<String, List<String>> titlesByAuthor() {
        return getAll().stream()
                .collect(Collectors.groupingBy(Book::getAuthor, TreeMap::new,
                        Collectors.mapping(Book::getTitle, Collectors.toList())));
    }

    @CacheEvict(value = "books", allEntries = true) // data changed -> clear cache
    public Book create(BookRequest request) {
        Book saved = repository.save(new Book(request.title(), request.author(), request.price(), request.stock()));
        log.info("Book added: {} by {}", saved.getTitle(), saved.getAuthor());
        return saved;
    }

    @CacheEvict(value = "books", allEntries = true)
    public void delete(String id) {
        repository.delete(getById(id));
        log.info("Book deleted: {}", id);
    }

    /** Called by the Kafka listener when an order is placed. */
    @CacheEvict(value = "books", allEntries = true)
    public void reduceStock(String bookId, int quantity) {
        repository.findById(bookId).ifPresentOrElse(book -> {
            book.setStock(Math.max(0, book.getStock() - quantity));
            repository.save(book);
            log.info("Stock for '{}' is now {}", book.getTitle(), book.getStock());
        }, () -> log.warn("Order event for unknown book {}", bookId));
    }
}
