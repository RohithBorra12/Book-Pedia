package com.bookstore.book.config;

import com.bookstore.book.model.Book;
import com.bookstore.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final BookRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            // List.of (Java 9)
            repository.saveAll(List.of(
                    new Book("Clean Code", "Robert C. Martin", 450.0, 10),
                    new Book("Effective Java", "Joshua Bloch", 650.0, 8),
                    new Book("Designing Data-Intensive Applications", "Martin Kleppmann", 900.0, 5),
                    new Book("Spring in Action", "Craig Walls", 550.0, 12),
                    new Book("The Pragmatic Programmer", "Andrew Hunt", 500.0, 7)));
            log.info("Seeded 5 demo books");
        }
    }
}
