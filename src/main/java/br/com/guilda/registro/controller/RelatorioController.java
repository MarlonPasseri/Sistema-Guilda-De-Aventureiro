package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.MissaoMetricaResponse;
import br.com.guilda.registro.dto.RankingParticipacaoResponse;
import br.com.guilda.registro.service.RelatorioService;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@Validated
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/aventureiros/ranking")
    public List<RankingParticipacaoResponse> rankingParticipacao(
        @RequestParam @Positive(message = "organizacaoId deve ser maior que 0") Long organizacaoId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim,
        @RequestParam(required = false) String statusMissao
    ) {
        return relatorioService.rankingParticipacao(organizacaoId, inicio, fim, statusMissao);
    }

    @GetMapping("/missoes")
    public List<MissaoMetricaResponse> relatorioMissoes(
        @RequestParam @Positive(message = "organizacaoId deve ser maior que 0") Long organizacaoId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim
    ) {
        return relatorioService.relatorioMissoes(organizacaoId, inicio, fim);
    }
}
