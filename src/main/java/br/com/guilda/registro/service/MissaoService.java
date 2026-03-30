package br.com.guilda.registro.service;

import br.com.guilda.registro.audit.domain.Organizacao;
import br.com.guilda.registro.audit.repository.OrganizacaoRepository;
import br.com.guilda.registro.domain.Aventureiro;
import br.com.guilda.registro.domain.Missao;
import br.com.guilda.registro.domain.NivelPerigoMissao;
import br.com.guilda.registro.domain.PapelMissao;
import br.com.guilda.registro.domain.ParticipacaoMissao;
import br.com.guilda.registro.domain.StatusMissao;
import br.com.guilda.registro.dto.CreateMissaoRequest;
import br.com.guilda.registro.dto.MissaoDetalheResponse;
import br.com.guilda.registro.dto.MissaoResumoResponse;
import br.com.guilda.registro.dto.PageResult;
import br.com.guilda.registro.dto.ParticipacaoMissaoRequest;
import br.com.guilda.registro.dto.ParticipacaoMissaoResponse;
import br.com.guilda.registro.exception.ApiException;
import br.com.guilda.registro.repository.AventureiroRepository;
import br.com.guilda.registro.repository.MissaoRepository;
import br.com.guilda.registro.repository.ParticipacaoMissaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class MissaoService {

    private final MissaoRepository missaoRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final AventureiroRepository aventureiroRepository;
    private final ParticipacaoMissaoRepository participacaoRepository;

    public MissaoService(
        MissaoRepository missaoRepository,
        OrganizacaoRepository organizacaoRepository,
        AventureiroRepository aventureiroRepository,
        ParticipacaoMissaoRepository participacaoRepository
    ) {
        this.missaoRepository = missaoRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.aventureiroRepository = aventureiroRepository;
        this.participacaoRepository = participacaoRepository;
    }

    public MissaoDetalheResponse criar(CreateMissaoRequest request) {
        Organizacao organizacao = organizacaoRepository.findById(request.getOrganizacaoId())
            .orElseThrow(() -> ApiException.notFound("Organizacao com id " + request.getOrganizacaoId() + " nao encontrada"));

        Missao missao = new Missao();
        missao.setOrganizacao(organizacao);
        missao.setTitulo(request.getTitulo().trim());
        missao.setNivelPerigo(parseNivelPerigo(request.getNivelPerigo()));
        missao.setStatus(parseStatus(request.getStatus()));
        missao.setDataInicio(request.getDataInicio());
        missao.setDataTermino(request.getDataTermino());

        Missao salva = missaoRepository.save(missao);
        return detalhar(organizacao.getId(), salva.getId());
    }

    @Transactional(readOnly = true)
    public PageResult<MissaoResumoResponse> listar(
        Long organizacaoId,
        String status,
        String nivelPerigo,
        OffsetDateTime dataDe,
        OffsetDateTime dataAte,
        int page,
        int size,
        String sortBy,
        String direction
    ) {
        Specification<Missao> spec = pertenceAOrganizacao(organizacaoId);

        StatusMissao statusFiltro = parseStatusOpcional(status);
        if (statusFiltro != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("status"), statusFiltro));
        }
        NivelPerigoMissao nivelPerigoFiltro = parseNivelPerigoOpcional(nivelPerigo);
        if (nivelPerigoFiltro != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("nivelPerigo"), nivelPerigoFiltro));
        }
        if (dataDe != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("createdAt"), dataDe));
        }
        if (dataAte != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("createdAt"), dataAte));
        }

        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, direction, "createdAt"));
        Page<MissaoResumoResponse> resultado = missaoRepository.findAll(spec, pageable).map(this::toResumo);
        return new PageResult<>(
            resultado.getContent(),
            resultado.getTotalElements(),
            resultado.getNumber(),
            resultado.getSize(),
            resultado.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public MissaoDetalheResponse detalhar(Long organizacaoId, Long missaoId) {
        Missao missao = obterMissao(organizacaoId, missaoId);
        return toDetalhe(missao);
    }

    public MissaoDetalheResponse adicionarParticipacao(Long organizacaoId, Long missaoId, ParticipacaoMissaoRequest request) {
        Missao missao = obterMissao(organizacaoId, missaoId);
        Aventureiro aventureiro = aventureiroRepository.findDetailedByIdAndOrganizacao_Id(request.getAventureiroId(), organizacaoId)
            .orElseThrow(() -> ApiException.notFound("Aventureiro com id " + request.getAventureiroId() + " nao encontrado para a organizacao " + organizacaoId));

        if (!missao.aceitaParticipantes()) {
            throw ApiException.invalid(List.of("A missao nao aceita participantes no status atual"));
        }
        if (!aventureiro.isAtivo()) {
            throw ApiException.invalid(List.of("Aventureiro inativo nao pode participar de novas missoes"));
        }
        if (!missao.getOrganizacao().getId().equals(aventureiro.getOrganizacao().getId())) {
            throw ApiException.invalid(List.of("Nao e permitido relacionamento cruzado entre organizacoes"));
        }
        if (participacaoRepository.existsByMissao_IdAndAventureiro_Id(missaoId, aventureiro.getId())) {
            throw ApiException.conflict("O aventureiro ja participa desta missao");
        }

        ParticipacaoMissao participacao = new ParticipacaoMissao();
        participacao.setPapelNaMissao(parsePapel(request.getPapelNaMissao()));
        participacao.setRecompensaOuro(request.getRecompensaOuro() == null ? BigDecimal.ZERO : request.getRecompensaOuro());
        participacao.setDestaque(Boolean.TRUE.equals(request.getDestaque()));
        missao.adicionarParticipacao(participacao);
        aventureiro.adicionarParticipacao(participacao);

        missaoRepository.saveAndFlush(missao);
        return detalhar(organizacaoId, missaoId);
    }

    private Missao obterMissao(Long organizacaoId, Long missaoId) {
        return missaoRepository.findDetailedByIdAndOrganizacao_Id(missaoId, organizacaoId)
            .orElseThrow(() -> ApiException.notFound("Missao com id " + missaoId + " nao encontrada para a organizacao " + organizacaoId));
    }

    private Specification<Missao> pertenceAOrganizacao(Long organizacaoId) {
        return (root, query, builder) -> builder.equal(root.get("organizacao").get("id"), organizacaoId);
    }

    private NivelPerigoMissao parseNivelPerigo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.invalid(List.of("nivelPerigo invalido"));
        }
        try {
            return NivelPerigoMissao.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.invalid(List.of("nivelPerigo invalido"));
        }
    }

    private NivelPerigoMissao parseNivelPerigoOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        return parseNivelPerigo(valor);
    }

    private StatusMissao parseStatus(String valor) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.invalid(List.of("status invalido"));
        }
        try {
            return StatusMissao.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.invalid(List.of("status invalido"));
        }
    }

    private StatusMissao parseStatusOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        return parseStatus(valor);
    }

    private PapelMissao parsePapel(String valor) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.invalid(List.of("papelNaMissao invalido"));
        }
        try {
            return PapelMissao.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.invalid(List.of("papelNaMissao invalido"));
        }
    }

    private Sort buildSort(String sortBy, String direction, String defaultProperty) {
        String property = switch (sortBy == null ? defaultProperty : sortBy) {
            case "titulo" -> "titulo";
            case "createdAt" -> "createdAt";
            case "dataInicio" -> "dataInicio";
            default -> throw ApiException.invalid(List.of("ordenacao invalida"));
        };
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, property);
    }

    private MissaoResumoResponse toResumo(Missao missao) {
        return new MissaoResumoResponse(
            missao.getId(),
            missao.getTitulo(),
            missao.getStatus().name(),
            missao.getNivelPerigo().name(),
            missao.getCreatedAt(),
            missao.getDataInicio(),
            missao.getDataTermino()
        );
    }

    private MissaoDetalheResponse toDetalhe(Missao missao) {
        List<ParticipacaoMissaoResponse> participantes = missao.getParticipacoes().stream()
            .map(participacao -> new ParticipacaoMissaoResponse(
                participacao.getAventureiro().getId(),
                participacao.getAventureiro().getNome(),
                participacao.getPapelNaMissao().name(),
                participacao.getRecompensaOuro(),
                participacao.isDestaque(),
                participacao.getDataRegistro()
            ))
            .toList();

        return new MissaoDetalheResponse(
            missao.getId(),
            missao.getOrganizacao().getId(),
            missao.getTitulo(),
            missao.getStatus().name(),
            missao.getNivelPerigo().name(),
            missao.getCreatedAt(),
            missao.getDataInicio(),
            missao.getDataTermino(),
            participantes
        );
    }
}
