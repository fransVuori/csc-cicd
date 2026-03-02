package com.example.demo.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Varasto kirjoille, joka tarjoaa tietokantaoperaatiot
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}