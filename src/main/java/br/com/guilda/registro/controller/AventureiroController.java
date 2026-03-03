package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.AventureiroDetalheResponse;
import br.com.guilda.registro.dto.AventureiroResumoResponse;
import br.com.guilda.registro.dto.CompanheiroRequest;
import br.com.guilda.registro.dto.CreateAventureiroRequest;
import br.com.guilda.registro.dto.PageResult;
import br.com.guilda.registro.dto.UpdateAventureiroRequest;
import br.com.guilda.registro.service.AventureiroService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("/aventureiros")
public class AventureiroController {

    private final AventureiroService service;

    public AventureiroController(AventureiroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AventureiroDetalheResponse> registrar(
        @Valid @RequestBody CreateAventureiroRequest request,
        UriComponentsBuilder uriBuilder
    ) {
        AventureiroDetalheResponse criado = service.registrar(request);
        URI location = uriBuilder.path("/aventureiros/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<AventureiroResumoResponse>> listar(
        @RequestParam(required = false) String classe,
        @RequestParam(required = false) Boolean ativo,
        @RequestParam(required = false) @Min(value = 1, message = "nivelMinimo deve ser maior ou igual a 1") Integer nivelMinimo,
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "page nao pode ser negativo") Integer page,
        @RequestParam(defaultValue = "10") @Min(value = 1, message = "size deve estar entre 1 e 50")
        @Max(value = 50, message = "size deve estar entre 1 e 50") Integer size
    ) {
        PageResult<AventureiroResumoResponse> resultado = service.listar(classe, ativo, nivelMinimo, page, size);

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
    public AventureiroDetalheResponse consultarPorId(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id
    ) {
        return service.consultarPorId(id);
    }

    @PutMapping("/{id}")
    public AventureiroDetalheResponse atualizar(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id,
        @Valid @RequestBody UpdateAventureiroRequest request
    ) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/encerrar-vinculo")
    public AventureiroDetalheResponse encerrarVinculo(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id
    ) {
        return service.encerrarVinculo(id);
    }

    @PatchMapping("/{id}/recrutar-novamente")
    public AventureiroDetalheResponse recrutarNovamente(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id
    ) {
        return service.recrutarNovamente(id);
    }

    @PutMapping("/{id}/companheiro")
    public AventureiroDetalheResponse definirOuSubstituirCompanheiro(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id,
        @Valid @RequestBody CompanheiroRequest request
    ) {
        return service.definirOuSubstituirCompanheiro(id, request);
    }

    @DeleteMapping("/{id}/companheiro")
    public ResponseEntity<Void> removerCompanheiro(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id
    ) {
        service.removerCompanheiro(id);
        return ResponseEntity.noContent().build();
    }
}
