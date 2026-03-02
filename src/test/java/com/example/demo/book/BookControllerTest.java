package com.example.demo.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// Integraatiotesti Spring Boot -sovellukselle
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookControllerTest {

    // Injektoidaan MockMvc testejä varten
    @Autowired
    private MockMvc mockMvc;

    // Testaa kirjan luomista ja hakemista
    @Test
    public void shouldCreateAndReturnBook() throws Exception {
        // Luodaan JSON-merkkijono uudelle kirjalle
        String newBookJson = "{\"title\":\"Testikirja\", \"author\":\"Testaaja\"}";

        // Lähetetään POST-pyyntö uuden kirjan luomiseksi
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newBookJson))
                // Varmistetaan, että vastaus on OK (200)
                .andExpect(status().isOk())
                // Varmistetaan, että vastauksen title-kenttä on oikea
                .andExpect(jsonPath("$.title").value("Testikirja"));

        // Lähetetään GET-pyyntö kaikkien kirjojen hakemiseksi
        mockMvc.perform(get("/api/books"))
                // Varmistetaan, että vastaus on OK (200)
                .andExpect(status().isOk())
                // Varmistetaan, että ensimmäisen kirjan title on oikea
                .andExpect(jsonPath("$[0].title").value("Testikirja"));
    }
}