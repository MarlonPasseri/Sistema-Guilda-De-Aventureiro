package br.com.guilda.registro.service;

import br.com.guilda.registro.audit.domain.Organizacao;
import br.com.guilda.registro.audit.domain.Usuario;
import br.com.guilda.registro.audit.repository.OrganizacaoRepository;
import br.com.guilda.registro.audit.repository.UsuarioRepository;
import br.com.guilda.registro.domain.Aventureiro;
import br.com.guilda.registro.domain.ClasseAventureiro;
import br.com.guilda.registro.domain.Companheiro;
import br.com.guilda.registro.domain.EspecieCompanheiro;
import br.com.guilda.registro.domain.ParticipacaoMissao;
import br.com.guilda.registro.dto.AventureiroDetalheResponse;
import br.com.guilda.registro.dto.AventureiroResumoResponse;
import br.com.guilda.registro.dto.CompanheiroRequest;
import br.com.guilda.registro.dto.CompanheiroResponse;
import br.com.guilda.registro.dto.CreateAventureiroRequest;
import br.com.guilda.registro.dto.PageResult;
import br.com.guilda.registro.dto.UltimaMissaoResumoResponse;
import br.com.guilda.registro.dto.UpdateAventureiroRequest;
import br.com.guilda.registro.exception.ApiException;
import br.com.guilda.registro.repository.AventureiroRepository;
import br.com.guilda.registro.repository.ParticipacaoMissaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class AventureiroService {

    private final AventureiroRepository repository;
    private final OrganizacaoRepository organizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParticipacaoMissaoRepository participacaoRepository;

    public AventureiroService(
        AventureiroRepository repository,
        OrganizacaoRepository organizacaoRepository,
        UsuarioRepository usuarioRepository,
        ParticipacaoMissaoRepository participacaoRepository
    ) {
        this.repository = repository;
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.participacaoRepository = participacaoRepository;
    }

    public AventureiroDetalheResponse registrar(CreateAventureiroRequest request) {
        Organizacao organizacao = obterOrganizacao(request.getOrganizacaoId());
        Usuario usuarioCadastro = obterUsuario(request.getUsuarioCadastroId());
        validarMesmoEscopo(organizacao.getId(), usuarioCadastro.getOrganizacao().getId(), "usuario de cadastro");

        Aventureiro aventureiro = new Aventureiro();
        aventureiro.setOrganizacao(organizacao);
        aventureiro.setUsuarioCadastro(usuarioCadastro);
        aventureiro.setNome(request.getNome().trim());
        aventureiro.setClasse(parseClasseObrigatoria(request.getClasse()));
        aventureiro.setNivel(request.getNivel());
        aventureiro.setAtivo(true);

        Aventureiro salvo = repository.save(aventureiro);
        return consultarPorId(organizacao.getId(), salvo.getId());
    }

    @Transactional(readOnly = true)
    public PageResult<AventureiroResumoResponse> listar(
        Long organizacaoId,
        String classe,
        Boolean ativo,
        Integer nivelMinimo,
        int page,
        int size,
        String sortBy,
        String direction
    ) {
        Specification<Aventureiro> spec = pertenceAOrganizacao(organizacaoId);

        ClasseAventureiro classeFiltro = parseClasseOpcional(classe);
        if (classeFiltro != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("classe"), classeFiltro));
        }
        if (ativo != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("ativo"), ativo));
        }
        if (nivelMinimo != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("nivel"), nivelMinimo));
        }

        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, direction, "nome"));
        Page<AventureiroResumoResponse> resultado = repository.findAll(spec, pageable).map(this::toResumo);
        return new PageResult<>(
            resultado.getContent(),
            resultado.getTotalElements(),
            resultado.getNumber(),
            resultado.getSize(),
            resultado.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PageResult<AventureiroResumoResponse> buscarPorNome(
        Long organizacaoId,
        String nome,
        int page,
        int size,
        String sortBy,
        String direction
    ) {
        if (nome == null || nome.isBlank()) {
            throw ApiException.invalid(List.of("nome da busca e obrigatorio"));
        }

        Specification<Aventureiro> spec = pertenceAOrganizacao(organizacaoId)
            .and((root, query, builder) -> builder.like(
                builder.lower(root.get("nome")),
                "%" + nome.trim().toLowerCase(Locale.ROOT) + "%"
            ));

        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, direction, "nome"));
        Page<AventureiroResumoResponse> resultado = repository.findAll(spec, pageable).map(this::toResumo);
        return new PageResult<>(
            resultado.getContent(),
            resultado.getTotalElements(),
            resultado.getNumber(),
            resultado.getSize(),
            resultado.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AventureiroDetalheResponse consultarPorId(Long organizacaoId, Long id) {
        Aventureiro aventureiro = obterAventureiro(organizacaoId, id);
        long totalParticipacoes = participacaoRepository.countByAventureiro_Id(id);
        Optional<ParticipacaoMissao> ultimaParticipacao = participacaoRepository.findFirstByAventureiro_IdOrderByDataRegistroDesc(id);
        return toDetalhe(aventureiro, totalParticipacoes, ultimaParticipacao.orElse(null));
    }

    public AventureiroDetalheResponse atualizar(Long organizacaoId, Long id, UpdateAventureiroRequest request) {
        Aventureiro existente = obterAventureiro(organizacaoId, id);
        existente.setNome(request.getNome().trim());
        existente.setClasse(parseClasseObrigatoria(request.getClasse()));
        existente.setNivel(request.getNivel());
        return consultarPorId(organizacaoId, existente.getId());
    }

    public AventureiroDetalheResponse encerrarVinculo(Long organizacaoId, Long id) {
        Aventureiro existente = obterAventureiro(organizacaoId, id);
        existente.setAtivo(false);
        return consultarPorId(organizacaoId, existente.getId());
    }

    public AventureiroDetalheResponse recrutarNovamente(Long organizacaoId, Long id) {
        Aventureiro existente = obterAventureiro(organizacaoId, id);
        existente.setAtivo(true);
        return consultarPorId(organizacaoId, existente.getId());
    }

    public AventureiroDetalheResponse definirOuSubstituirCompanheiro(Long organizacaoId, Long id, CompanheiroRequest request) {
        Aventureiro existente = obterAventureiro(organizacaoId, id);
        Companheiro companheiro = new Companheiro(
            request.getNome().trim(),
            parseEspecieObrigatoria(request.getEspecie()),
            request.getLealdade()
        );
        existente.definirCompanheiro(companheiro);
        return consultarPorId(organizacaoId, existente.getId());
    }

    public void removerCompanheiro(Long organizacaoId, Long id) {
        Aventureiro existente = obterAventureiro(organizacaoId, id);
        existente.removerCompanheiro();
    }

    private Organizacao obterOrganizacao(Long organizacaoId) {
        return organizacaoRepository.findById(organizacaoId)
            .orElseThrow(() -> ApiException.notFound("Organizacao com id " + organizacaoId + " nao encontrada"));
    }

    private Usuario obterUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> ApiException.notFound("Usuario com id " + usuarioId + " nao encontrado"));
    }

    private Aventureiro obterAventureiro(Long organizacaoId, Long id) {
        return repository.findDetailedByIdAndOrganizacao_Id(id, organizacaoId)
            .orElseThrow(() -> ApiException.notFound("Aventureiro com id " + id + " nao encontrado para a organizacao " + organizacaoId));
    }

    private Specification<Aventureiro> pertenceAOrganizacao(Long organizacaoId) {
        return (root, query, builder) -> builder.equal(root.get("organizacao").get("id"), organizacaoId);
    }

    private void validarMesmoEscopo(Long organizacaoEsperada, Long organizacaoAtual, String recurso) {
        if (!organizacaoEsperada.equals(organizacaoAtual)) {
            throw ApiException.invalid(List.of("O " + recurso + " deve pertencer a mesma organizacao"));
        }
    }

    private ClasseAventureiro parseClasseObrigatoria(String valor) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.invalid(List.of("classe invalida"));
        }
        try {
            return ClasseAventureiro.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.invalid(List.of("classe invalida"));
        }
    }

    private ClasseAventureiro parseClasseOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        return parseClasseObrigatoria(valor);
    }

    private EspecieCompanheiro parseEspecieObrigatoria(String valor) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.invalid(List.of("especie invalida"));
        }
        try {
            return EspecieCompanheiro.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.invalid(List.of("especie invalida"));
        }
    }

    private Sort buildSort(String sortBy, String direction, String defaultProperty) {
        String property = switch (sortBy == null ? defaultProperty : sortBy) {
            case "nome" -> "nome";
            case "nivel" -> "nivel";
            default -> throw ApiException.invalid(List.of("ordenacao invalida"));
        };
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(sortDirection, property);
    }

    private AventureiroResumoResponse toResumo(Aventureiro aventureiro) {
        return new AventureiroResumoResponse(
            aventureiro.getId(),
            aventureiro.getNome(),
            aventureiro.getClasse().name(),
            aventureiro.getNivel(),
            aventureiro.isAtivo()
        );
    }

    private AventureiroDetalheResponse toDetalhe(
        Aventureiro aventureiro,
        long totalParticipacoes,
        ParticipacaoMissao ultimaParticipacao
    ) {
        UltimaMissaoResumoResponse ultimaMissao = null;
        if (ultimaParticipacao != null) {
            ultimaMissao = new UltimaMissaoResumoResponse(
                ultimaParticipacao.getMissao().getId(),
                ultimaParticipacao.getMissao().getTitulo(),
                ultimaParticipacao.getMissao().getStatus().name(),
                ultimaParticipacao.getDataRegistro()
            );
        }

        return new AventureiroDetalheResponse(
            aventureiro.getId(),
            aventureiro.getOrganizacao().getId(),
            aventureiro.getUsuarioCadastro().getId(),
            aventureiro.getNome(),
            aventureiro.getClasse().name(),
            aventureiro.getNivel(),
            aventureiro.isAtivo(),
            toCompanheiro(aventureiro.getCompanheiro()),
            totalParticipacoes,
            ultimaMissao
        );
    }

    private CompanheiroResponse toCompanheiro(Companheiro companheiro) {
        if (companheiro == null) {
            return null;
        }
        return new CompanheiroResponse(
            companheiro.getNome(),
            companheiro.getEspecie().name(),
            companheiro.getLealdade()
        );
    }
}
