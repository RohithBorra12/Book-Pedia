package com.bookstore.book.service;

import com.bookstore.book.model.Book;
import com.bookstore.book.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository repository;

    @InjectMocks
    BookService bookService;

    @Test
    void reduceStockNeverGoesBelowZero() {
        Book book = new Book("Clean Code", "Robert C. Martin", 450.0, 2);
        when(repository.findById("1")).thenReturn(Optional.of(book));

        bookService.reduceStock("1", 5);

        assertEquals(0, book.getStock());
        verify(repository).save(book);
    }

    @Test
    void getByIdThrowsWhenBookMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> bookService.getById("missing"));
    }
}
