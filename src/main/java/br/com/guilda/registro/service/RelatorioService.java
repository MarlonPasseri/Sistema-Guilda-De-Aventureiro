package br.com.guilda.registro.service;

import br.com.guilda.registro.domain.StatusMissao;
import br.com.guilda.registro.dto.MissaoMetricaResponse;
import br.com.guilda.registro.dto.RankingParticipacaoResponse;
import br.com.guilda.registro.exception.ApiException;
import br.com.guilda.registro.repository.ParticipacaoMissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private static final OffsetDateTime INICIO_PADRAO = OffsetDateTime.parse("1900-01-01T00:00:00Z");
    private static final OffsetDateTime FIM_PADRAO = OffsetDateTime.parse("2999-12-31T23:59:59Z");

    private final ParticipacaoMissaoRepository participacaoRepository;

    public RelatorioService(ParticipacaoMissaoRepository participacaoRepository) {
        this.participacaoRepository = participacaoRepository;
    }

    public List<RankingParticipacaoResponse> rankingParticipacao(
        Long organizacaoId,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        String statusMissao
    ) {
        OffsetDateTime inicioNormalizado = inicio == null ? INICIO_PADRAO : inicio;
        OffsetDateTime fimNormalizado = fim == null ? FIM_PADRAO : fim;
        StatusMissao status = parseStatusOpcional(statusMissao);

        if (status == null) {
            return participacaoRepository.gerarRanking(organizacaoId, inicioNormalizado, fimNormalizado);
        }
        return participacaoRepository.gerarRankingPorStatus(organizacaoId, inicioNormalizado, fimNormalizado, status);
    }

    public List<MissaoMetricaResponse> relatorioMissoes(
        Long organizacaoId,
        OffsetDateTime inicio,
        OffsetDateTime fim
    ) {
        OffsetDateTime inicioNormalizado = inicio == null ? INICIO_PADRAO : inicio;
        OffsetDateTime fimNormalizado = fim == null ? FIM_PADRAO : fim;
        return participacaoRepository.relatorioMissoes(organizacaoId, inicioNormalizado, fimNormalizado);
    }

    private StatusMissao parseStatusOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return StatusMissao.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.invalid(List.of("statusMissao invalido"));
        }
    }
}
