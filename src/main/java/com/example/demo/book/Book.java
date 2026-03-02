package com.example.demo.book;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Kirja-entiteetti tietokantaan
@Entity
public class Book {
    
    // Pääavain, joka luodaan automaattisesti
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;      // Kirjan otsikko
    private String author;     // Kirjan kirjoittaja

    // Tyhjä konstruktori
    public Book() {}

    // Konstruktori otsikon ja kirjoittajan kanssa
    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    // Getterit ja setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}