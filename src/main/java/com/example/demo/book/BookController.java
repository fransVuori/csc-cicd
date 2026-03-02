package com.example.demo.book;

import org.springframework.web.bind.annotation.*;
import java.util.List;

// REST-kontrolleri kirjojen hallintaa varten
@RestController
@RequestMapping("/api/books")
public class BookController {

    // Repositorio kirjojen tietokantaoperaatioita varten
    private final BookRepository repository;

    // Konstruktori riippuvuuksien injektointia varten
    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    // GET-pyyntö kaikkien kirjojen hakemiseksi
    @GetMapping
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    // POST-pyyntö uuden kirjan luomiseksi
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return repository.save(book);
    }
}