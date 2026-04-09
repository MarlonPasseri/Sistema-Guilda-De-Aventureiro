package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.AgregacaoQuantidadeResponse;
import br.com.guilda.registro.dto.FaixaPrecoResponse;
import br.com.guilda.registro.dto.PrecoMedioResponse;
import br.com.guilda.registro.dto.ProdutoMarketplaceResponse;
import br.com.guilda.registro.service.ProdutoMarketplaceService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Validated
@RequestMapping("/produtos")
public class ProdutoMarketplaceController {

    private final ProdutoMarketplaceService produtoMarketplaceService;

    public ProdutoMarketplaceController(ProdutoMarketplaceService produtoMarketplaceService) {
        this.produtoMarketplaceService = produtoMarketplaceService;
    }

    @GetMapping("/busca/nome")
    public List<ProdutoMarketplaceResponse> buscarPorNome(@RequestParam @NotBlank String termo) {
        return produtoMarketplaceService.buscarPorNome(termo);
    }

    @GetMapping("/busca/descricao")
    public List<ProdutoMarketplaceResponse> buscarPorDescricao(@RequestParam @NotBlank String termo) {
        return produtoMarketplaceService.buscarPorDescricao(termo);
    }

    @GetMapping("/busca/frase")
    public List<ProdutoMarketplaceResponse> buscarPorFrase(@RequestParam @NotBlank String termo) {
        return produtoMarketplaceService.buscarFraseExata(termo);
    }

    @GetMapping("/busca/fuzzy")
    public List<ProdutoMarketplaceResponse> buscarFuzzy(@RequestParam @NotBlank String termo) {
        return produtoMarketplaceService.buscarFuzzyPorNome(termo);
    }

    @GetMapping("/busca/multicampos")
    public List<ProdutoMarketplaceResponse> buscarMulticampos(@RequestParam @NotBlank String termo) {
        return produtoMarketplaceService.buscarEmMultiplosCampos(termo);
    }

    @GetMapping("/busca/com-filtro")
    public List<ProdutoMarketplaceResponse> buscarComFiltro(
        @RequestParam @NotBlank String termo,
        @RequestParam @NotBlank String categoria
    ) {
        return produtoMarketplaceService.buscarPorDescricaoComFiltroCategoria(termo, categoria);
    }

    @GetMapping("/busca/faixa-preco")
    public List<ProdutoMarketplaceResponse> buscarPorFaixaDePreco(
        @RequestParam @DecimalMin(value = "0.0", inclusive = true) BigDecimal min,
        @RequestParam @DecimalMin(value = "0.0", inclusive = true) BigDecimal max
    ) {
        return produtoMarketplaceService.buscarPorFaixaDePreco(min, max);
    }

    @GetMapping("/busca/avancada")
    public List<ProdutoMarketplaceResponse> buscarAvancada(
        @RequestParam @NotBlank String categoria,
        @RequestParam @NotBlank String raridade,
        @RequestParam @DecimalMin(value = "0.0", inclusive = true) BigDecimal min,
        @RequestParam @DecimalMin(value = "0.0", inclusive = true) BigDecimal max
    ) {
        return produtoMarketplaceService.buscarAvancada(categoria, raridade, min, max);
    }

    @GetMapping("/agregacoes/por-categoria")
    public List<AgregacaoQuantidadeResponse> quantidadePorCategoria() {
        return produtoMarketplaceService.quantidadePorCategoria();
    }

    @GetMapping("/agregacoes/por-raridade")
    public List<AgregacaoQuantidadeResponse> quantidadePorRaridade() {
        return produtoMarketplaceService.quantidadePorRaridade();
    }

    @GetMapping("/agregacoes/preco-medio")
    public PrecoMedioResponse precoMedio() {
        return produtoMarketplaceService.precoMedio();
    }

    @GetMapping("/agregacoes/faixas-preco")
    public List<FaixaPrecoResponse> agruparPorFaixasDePreco() {
        return produtoMarketplaceService.agruparPorFaixasDePreco();
    }
}
