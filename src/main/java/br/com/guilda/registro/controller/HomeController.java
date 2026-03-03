package br.com.guilda.registro.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/status")
public class HomeController {

    @GetMapping
    public Map<String, String> home() {
        return Map.of(
            "mensagem", "API da Guilda em execucao",
            "listar", "GET /aventureiros",
            "consultarPorId", "GET /aventureiros/{id}"
        );
    }
}
