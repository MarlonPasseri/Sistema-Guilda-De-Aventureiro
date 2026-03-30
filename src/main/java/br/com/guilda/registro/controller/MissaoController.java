package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.CreateMissaoRequest;
import br.com.guilda.registro.dto.MissaoDetalheResponse;
import br.com.guilda.registro.dto.MissaoResumoResponse;
import br.com.guilda.registro.dto.PageResult;
import br.com.guilda.registro.dto.ParticipacaoMissaoRequest;
import br.com.guilda.registro.service.MissaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@Validated
@RequestMapping("/missoes")
public class MissaoController {

    private final MissaoService missaoService;

    public MissaoController(MissaoService missaoService) {
        this.missaoService = missaoService;
    }

    @PostMapping
    public ResponseEntity<MissaoDetalheResponse> criar(
        @Valid @RequestBody CreateMissaoRequest request,
        UriComponentsBuilder uriBuilder
    ) {
        MissaoDetalheResponse criada = missaoService.criar(request);
        URI location = uriBuilder.path("/missoes/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<MissaoResumoResponse>> listar(
        @RequestParam @Positive(message = "organizacaoId deve ser maior que 0") Long organizacaoId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String nivelPerigo,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataDe,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataAte,
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "page nao pode ser negativo") Integer page,
        @RequestParam(defaultValue = "10") @Min(value = 1, message = "size deve estar entre 1 e 50")
        @Max(value = 50, message = "size deve estar entre 1 e 50") Integer size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        PageResult<MissaoResumoResponse> resultado = missaoService.listar(
            organizacaoId, status, nivelPerigo, dataDe, dataAte, page, size, sortBy, direction
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(resultado.totalCount()));
        headers.add("X-Page", String.valueOf(resultado.page()));
        headers.add("X-Size", String.valueOf(resultado.size()));
        headers.add("X-Total-Pages", String.valueOf(resultado.totalPages()));

        return ResponseEntity.ok()
            .headers(headers)
            .body(resultado.items());
    }

    @GetMapping("/{id}")
    public MissaoDetalheResponse detalhar(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id,
        @RequestParam @Positive(message = "organizacaoId deve ser maior que 0") Long organizacaoId
    ) {
        return missaoService.detalhar(organizacaoId, id);
    }

    @PostMapping("/{id}/participacoes")
    public MissaoDetalheResponse adicionarParticipacao(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id,
        @RequestParam @Positive(message = "organizacaoId deve ser maior que 0") Long organizacaoId,
        @Valid @RequestBody ParticipacaoMissaoRequest request
    ) {
        return missaoService.adicionarParticipacao(organizacaoId, id, request);
    }
}
