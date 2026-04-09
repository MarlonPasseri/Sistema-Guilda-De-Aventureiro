package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.PainelTaticoMissaoResponse;
import br.com.guilda.registro.service.PainelTaticoMissaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/missoes")
public class PainelTaticoMissaoController {

    private final PainelTaticoMissaoService painelTaticoMissaoService;

    public PainelTaticoMissaoController(PainelTaticoMissaoService painelTaticoMissaoService) {
        this.painelTaticoMissaoService = painelTaticoMissaoService;
    }

    @GetMapping("/top15dias")
    public List<PainelTaticoMissaoResponse> listarTop15Dias() {
        return painelTaticoMissaoService.listarTopMissoesUltimos15Dias();
    }
}
