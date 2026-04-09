package br.com.guilda.registro.service;

import br.com.guilda.registro.dto.AgregacaoQuantidadeResponse;
import br.com.guilda.registro.dto.FaixaPrecoResponse;
import br.com.guilda.registro.dto.PrecoMedioResponse;
import br.com.guilda.registro.dto.ProdutoMarketplaceDocument;
import br.com.guilda.registro.dto.ProdutoMarketplaceResponse;
import br.com.guilda.registro.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Transactional(readOnly = true)
public class ProdutoMarketplaceService {

    private static final String INDICE_GUILDA_LOJA = "guilda_loja";
    private static final int TAMANHO_PADRAO_BUSCA = 20;

    private final RestClient elasticsearchApiClient;
    private final ObjectMapper objectMapper;
    private final HistoricoBuscaProdutoService historicoBuscaProdutoService;
    private final ConcurrentMap<String, String> exactFieldCache = new ConcurrentHashMap<>();

    public ProdutoMarketplaceService(
        RestClient elasticsearchApiClient,
        ObjectMapper objectMapper,
        HistoricoBuscaProdutoService historicoBuscaProdutoService
    ) {
        this.elasticsearchApiClient = elasticsearchApiClient;
        this.objectMapper = objectMapper;
        this.historicoBuscaProdutoService = historicoBuscaProdutoService;
    }

    public List<ProdutoMarketplaceResponse> buscarPorNome(String termo) {
        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("match", Map.of("nome", Map.of("query", normalizarTexto(termo))))
        ));
        historicoBuscaProdutoService.registrarBusca("nome", termo, null, null, null, null, produtos.size());
        return produtos;
    }

    public List<ProdutoMarketplaceResponse> buscarPorDescricao(String termo) {
        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("match", Map.of("descricao", Map.of("query", normalizarTexto(termo))))
        ));
        historicoBuscaProdutoService.registrarBusca("descricao", termo, null, null, null, null, produtos.size());
        return produtos;
    }

    public List<ProdutoMarketplaceResponse> buscarFraseExata(String termo) {
        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("match_phrase", Map.of("descricao", Map.of("query", normalizarTexto(termo))))
        ));
        historicoBuscaProdutoService.registrarBusca("frase", termo, null, null, null, null, produtos.size());
        return produtos;
    }

    public List<ProdutoMarketplaceResponse> buscarFuzzyPorNome(String termo) {
        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("match", Map.of("nome", Map.of(
                "query", normalizarTexto(termo),
                "fuzziness", "AUTO"
            )))
        ));
        historicoBuscaProdutoService.registrarBusca("fuzzy", termo, null, null, null, null, produtos.size());
        return produtos;
    }

    public List<ProdutoMarketplaceResponse> buscarEmMultiplosCampos(String termo) {
        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("multi_match", Map.of(
                "query", normalizarTexto(termo),
                "fields", List.of("nome", "descricao")
            ))
        ));
        historicoBuscaProdutoService.registrarBusca("multicampos", termo, null, null, null, null, produtos.size());
        return produtos;
    }

    public List<ProdutoMarketplaceResponse> buscarPorDescricaoComFiltroCategoria(String termo, String categoria) {
        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("bool", Map.of(
                "must", List.of(Map.of("match", Map.of("descricao", Map.of("query", normalizarTexto(termo))))),
                "filter", List.of(Map.of("term", Map.of(resolverCampoExato("categoria"), normalizarFiltroExato(categoria))))
            ))
        ));
        historicoBuscaProdutoService.registrarBusca("descricao_categoria", termo, categoria, null, null, null, produtos.size());
        return produtos;
    }

    public List<ProdutoMarketplaceResponse> buscarPorFaixaDePreco(BigDecimal min, BigDecimal max) {
        validarFaixaPreco(min, max);
        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("range", Map.of("preco", Map.of("gte", min, "lte", max))),
            "sort", List.of(Map.of("preco", Map.of("order", "asc")))
        ));
        historicoBuscaProdutoService.registrarBusca("faixa_preco", null, null, null, min, max, produtos.size());
        return produtos;
    }

    public List<ProdutoMarketplaceResponse> buscarAvancada(
        String categoria,
        String raridade,
        BigDecimal min,
        BigDecimal max
    ) {
        validarFaixaPreco(min, max);

        List<ProdutoMarketplaceResponse> produtos = executarBusca(Map.of(
            "size", TAMANHO_PADRAO_BUSCA,
            "query", Map.of("bool", Map.of(
                "filter", List.of(
                    Map.of("term", Map.of(resolverCampoExato("categoria"), normalizarFiltroExato(categoria))),
                    Map.of("term", Map.of(resolverCampoExato("raridade"), normalizarFiltroExato(raridade))),
                    Map.of("range", Map.of("preco", Map.of("gte", min, "lte", max)))
                )
            )),
            "sort", List.of(Map.of("preco", Map.of("order", "asc")))
        ));
        historicoBuscaProdutoService.registrarBusca("avancada", null, categoria, raridade, min, max, produtos.size());
        return produtos;
    }

    public List<AgregacaoQuantidadeResponse> quantidadePorCategoria() {
        return executarAgregacaoPorTermos("por_categoria", resolverCampoExato("categoria"));
    }

    public List<AgregacaoQuantidadeResponse> quantidadePorRaridade() {
        return executarAgregacaoPorTermos("por_raridade", resolverCampoExato("raridade"));
    }

    public PrecoMedioResponse precoMedio() {
        JsonNode response = executarConsulta(Map.of(
            "size", 0,
            "aggs", Map.of("preco_medio", Map.of("avg", Map.of("field", "preco")))
        ));

        JsonNode valueNode = response.path("aggregations").path("preco_medio").path("value");
        BigDecimal valor = valueNode.isNumber()
            ? BigDecimal.valueOf(valueNode.asDouble()).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return new PrecoMedioResponse(valor);
    }

    public List<FaixaPrecoResponse> agruparPorFaixasDePreco() {
        JsonNode response = executarConsulta(Map.of(
            "size", 0,
            "aggs", Map.of("faixas_preco", Map.of(
                "range", Map.of(
                    "field", "preco",
                    "ranges", List.of(
                        Map.of("key", "Abaixo de 100", "to", 100),
                        Map.of("key", "De 100 a 300", "from", 100, "to", 300),
                        Map.of("key", "De 300 a 700", "from", 300, "to", 700),
                        Map.of("key", "Acima de 700", "from", 700)
                    )
                )
            ))
        ));

        List<FaixaPrecoResponse> resultado = new ArrayList<>();
        JsonNode buckets = response.path("aggregations").path("faixas_preco").path("buckets");
        buckets.forEach(bucket -> resultado.add(new FaixaPrecoResponse(
            bucket.path("key").asText(),
            bucket.path("doc_count").asLong()
        )));
        return resultado;
    }

    private List<AgregacaoQuantidadeResponse> executarAgregacaoPorTermos(String nomeAgregacao, String campo) {
        JsonNode response = executarConsulta(Map.of(
            "size", 0,
            "aggs", Map.of(nomeAgregacao, Map.of(
                "terms", Map.of(
                    "field", campo,
                    "size", 20
                )
            ))
        ));

        List<AgregacaoQuantidadeResponse> resultado = new ArrayList<>();
        JsonNode buckets = response.path("aggregations").path(nomeAgregacao).path("buckets");
        buckets.forEach(bucket -> resultado.add(new AgregacaoQuantidadeResponse(
            bucket.path("key").asText(),
            bucket.path("doc_count").asLong()
        )));
        return resultado;
    }

    private List<ProdutoMarketplaceResponse> executarBusca(Map<String, Object> body) {
        JsonNode response = executarConsulta(body);
        List<ProdutoMarketplaceResponse> produtos = new ArrayList<>();
        JsonNode hits = response.path("hits").path("hits");
        hits.forEach(hit -> produtos.add(toResponse(hit)));
        return produtos;
    }

    private JsonNode executarConsulta(Map<String, Object> body) {
        try {
            return elasticsearchApiClient.post()
                .uri("/{indice}/_search", INDICE_GUILDA_LOJA)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw new ApiException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Servico de busca indisponivel",
                List.of("Nao foi possivel consultar o indice guilda_loja")
            );
        }
    }

    private ProdutoMarketplaceResponse toResponse(JsonNode hit) {
        try {
            ProdutoMarketplaceDocument documento = objectMapper.treeToValue(hit.path("_source"), ProdutoMarketplaceDocument.class);
            return new ProdutoMarketplaceResponse(
                hit.path("_id").asText(),
                documento.nome(),
                documento.descricao(),
                documento.categoria(),
                documento.raridade(),
                documento.preco()
            );
        } catch (Exception ex) {
            throw new ApiException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Falha ao processar resposta do Elasticsearch",
                List.of("Nao foi possivel converter os produtos retornados pela busca")
            );
        }
    }

    private String resolverCampoExato(String campoBase) {
        return exactFieldCache.computeIfAbsent(campoBase, this::buscarCampoExatoNoMapping);
    }

    private String buscarCampoExatoNoMapping(String campoBase) {
        try {
            JsonNode mapping = elasticsearchApiClient.get()
                .uri("/{indice}/_mapping", INDICE_GUILDA_LOJA)
                .retrieve()
                .body(JsonNode.class);

            JsonNode properties = mapping.path(INDICE_GUILDA_LOJA).path("mappings").path("properties");
            JsonNode fieldNode = properties.path(campoBase);
            if (fieldNode.isMissingNode()) {
                return campoBase;
            }

            if ("keyword".equals(fieldNode.path("type").asText())) {
                return campoBase;
            }

            JsonNode keywordField = fieldNode.path("fields").path("keyword");
            if (!keywordField.isMissingNode()) {
                return campoBase + ".keyword";
            }

            return campoBase;
        } catch (RestClientException ex) {
            return campoBase;
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.invalid(List.of("termo informado e invalido"));
        }
        return valor.trim();
    }

    private String normalizarFiltroExato(String valor) {
        return normalizarTexto(valor).toLowerCase(Locale.ROOT);
    }

    private void validarFaixaPreco(BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            throw ApiException.invalid(List.of("faixa de preco invalida"));
        }
        if (min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.invalid(List.of("faixa de preco invalida"));
        }
        if (min.compareTo(max) > 0) {
            throw ApiException.invalid(List.of("min nao pode ser maior que max"));
        }
    }
}
