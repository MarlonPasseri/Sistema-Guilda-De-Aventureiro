package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.AutoconfiguracaoDiagnosticoResponse;
import br.com.guilda.registro.dto.ConfiguracaoDinamicaResponse;
import br.com.guilda.registro.service.ConfiguracaoDinamicaService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/diagnosticos")
public class DiagnosticoController {

    private final ConfiguracaoDinamicaService configuracaoDinamicaService;

    public DiagnosticoController(ConfiguracaoDinamicaService configuracaoDinamicaService) {
        this.configuracaoDinamicaService = configuracaoDinamicaService;
    }

    @GetMapping("/autoconfiguracao/persistencia")
    public List<AutoconfiguracaoDiagnosticoResponse> diagnosticarPersistencia() {
        return configuracaoDinamicaService.diagnosticarPersistencia();
    }

    @GetMapping("/configuracoes")
    public ConfiguracaoDinamicaResponse consultarConfiguracoes() {
        return configuracaoDinamicaService.consultarConfiguracoes();
    }

    @PatchMapping("/configuracoes")
    public ConfiguracaoDinamicaResponse atualizarConfiguracoes(
        @RequestParam(required = false) @Min(value = 0, message = "ttlRankingSegundos nao pode ser negativo") Integer ttlRankingSegundos,
        @RequestParam(required = false) Boolean historicoMongoHabilitado
    ) {
        return configuracaoDinamicaService.atualizarConfiguracoes(ttlRankingSegundos, historicoMongoHabilitado);
    }
}
