package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.HistoricoBuscaProdutoResponse;
import br.com.guilda.registro.service.HistoricoBuscaProdutoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/produtos/buscas")
public class HistoricoBuscaProdutoController {

    private final HistoricoBuscaProdutoService historicoBuscaProdutoService;

    public HistoricoBuscaProdutoController(HistoricoBuscaProdutoService historicoBuscaProdutoService) {
        this.historicoBuscaProdutoService = historicoBuscaProdutoService;
    }

    @GetMapping("/historico")
    public List<HistoricoBuscaProdutoResponse> listarHistorico(
        @RequestParam(required = false) String tipoBusca
    ) {
        return historicoBuscaProdutoService.listarUltimasBuscas(tipoBusca);
    }
}
