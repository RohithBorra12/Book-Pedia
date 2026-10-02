package com.bookstore.book.repository;

import com.bookstore.book.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findByAuthorContainingIgnoreCase(String author);
}
