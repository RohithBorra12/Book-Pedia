package com.bookstore.book.controller;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.model.Book;
import com.bookstore.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping                                   // any logged-in user
    public List<Book> all() {
        return bookService.getAll();
    }

    @GetMapping("/{id}")
    public Book one(@PathVariable String id) {
        return bookService.getById(id);
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam String author) {
        return bookService.searchByAuthor(author);
    }

    @GetMapping("/by-author")
    public Map<String, List<String>> byAuthor() {
        return bookService.titlesByAuthor();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")             // ADMIN only
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@Valid @RequestBody BookRequest request) {
        return bookService.create(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        bookService.delete(id);
    }
}
