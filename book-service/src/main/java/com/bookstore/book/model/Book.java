package com.bookstore.book.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

// Stored in MongoDB collection "books". Serializable so Redis can cache it.
@Document("books")
@Getter
@Setter
@NoArgsConstructor
public class Book implements Serializable {

    @Id
    private String id;
    private String title;
    private String author;
    private double price;
    private int stock;

    public Book(String title, String author, double price, int stock) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }
}
