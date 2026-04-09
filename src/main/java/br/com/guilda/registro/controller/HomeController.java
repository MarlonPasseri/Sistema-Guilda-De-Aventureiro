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
            "auditUsuarios", "GET /audit/usuarios?organizacaoId=1",
            "auditRoles", "GET /audit/roles?organizacaoId=1",
            "aventuras", "GET /aventureiros?organizacaoId=1",
            "missoes", "GET /missoes?organizacaoId=1",
            "relatorios", "GET /relatorios/aventureiros/ranking?organizacaoId=1",
            "rankingRedis", "GET /missoes/top15dias",
            "historicoMongo", "GET /produtos/buscas/historico",
            "diagnosticos", "GET /diagnosticos/autoconfiguracao/persistencia"
        );
    }
}
