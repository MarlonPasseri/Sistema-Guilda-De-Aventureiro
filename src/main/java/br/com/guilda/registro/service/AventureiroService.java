package br.com.guilda.registro.service;

import br.com.guilda.registro.domain.Aventureiro;
import br.com.guilda.registro.domain.ClasseAventureiro;
import br.com.guilda.registro.domain.Companheiro;
import br.com.guilda.registro.domain.EspecieCompanheiro;
import br.com.guilda.registro.dto.AventureiroDetalheResponse;
import br.com.guilda.registro.dto.AventureiroResumoResponse;
import br.com.guilda.registro.dto.CompanheiroRequest;
import br.com.guilda.registro.dto.CompanheiroResponse;
import br.com.guilda.registro.dto.CreateAventureiroRequest;
import br.com.guilda.registro.dto.PageResult;
import br.com.guilda.registro.dto.UpdateAventureiroRequest;
import br.com.guilda.registro.exception.ApiException;
import br.com.guilda.registro.repository.AventureiroMemoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AventureiroService {

    private final AventureiroMemoriaRepository repository;

    public AventureiroService(AventureiroMemoriaRepository repository) {
        this.repository = repository;
    }

    public AventureiroDetalheResponse registrar(CreateAventureiroRequest request) {
        ClasseAventureiro classe = parseClasseObrigatoria(request.getClasse());
        Aventureiro criado = repository.criar(request.getNome().trim(), classe, request.getNivel());
        return toDetalhe(criado);
    }

    public PageResult<AventureiroResumoResponse> listar(String classe, Boolean ativo, Integer nivelMinimo, int page, int size) {
        ClasseAventureiro classeFiltro = parseClasseOpcional(classe);

        List<Aventureiro> filtrados = repository.listarTodosOrdenadosPorId().stream()
            .filter(aventureiro -> classeFiltro == null || aventureiro.getClasse() == classeFiltro)
            .filter(aventureiro -> ativo == null || aventureiro.isAtivo() == ativo)
            .filter(aventureiro -> nivelMinimo == null || aventureiro.getNivel() >= nivelMinimo)
            .toList();

        int totalCount = filtrados.size();
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / size);
        int fromIndex = page * size;

        List<AventureiroResumoResponse> items;
        if (fromIndex >= totalCount) {
            items = List.of();
        } else {
            int toIndex = Math.min(fromIndex + size, totalCount);
            items = filtrados.subList(fromIndex, toIndex).stream()
                .map(this::toResumo)
                .toList();
        }

        return new PageResult<>(items, totalCount, page, size, totalPages);
    }

    public AventureiroDetalheResponse consultarPorId(Long id) {
        return toDetalhe(obterPorId(id));
    }

    public AventureiroDetalheResponse atualizar(Long id, UpdateAventureiroRequest request) {
        Aventureiro existente = obterPorId(id);
        existente.setNome(request.getNome().trim());
        existente.setClasse(parseClasseObrigatoria(request.getClasse()));
        existente.setNivel(request.getNivel());
        return toDetalhe(existente);
    }

    public AventureiroDetalheResponse encerrarVinculo(Long id) {
        Aventureiro existente = obterPorId(id);
        existente.setAtivo(false);
        return toDetalhe(existente);
    }

    public AventureiroDetalheResponse recrutarNovamente(Long id) {
        Aventureiro existente = obterPorId(id);
        existente.setAtivo(true);
        return toDetalhe(existente);
    }

    public AventureiroDetalheResponse definirOuSubstituirCompanheiro(Long id, CompanheiroRequest request) {
        Aventureiro existente = obterPorId(id);
        Companheiro companheiro = new Companheiro(
            request.getNome().trim(),
            parseEspecieObrigatoria(request.getEspecie()),
            request.getLealdade()
        );
        existente.setCompanheiro(companheiro);
        return toDetalhe(existente);
    }

    public void removerCompanheiro(Long id) {
        Aventureiro existente = obterPorId(id);
        existente.setCompanheiro(null);
    }

    private Aventureiro obterPorId(Long id) {
        return repository.buscarPorId(id)
            .orElseThrow(() -> ApiException.notFound("Aventureiro com id " + id + " nao encontrado"));
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
        if (valor.isBlank()) {
            throw ApiException.invalid(List.of("classe invalida"));
        }
        try {
            return ClasseAventureiro.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.invalid(List.of("classe invalida"));
        }
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

    private AventureiroResumoResponse toResumo(Aventureiro aventureiro) {
        return new AventureiroResumoResponse(
            aventureiro.getId(),
            aventureiro.getNome(),
            aventureiro.getClasse().name(),
            aventureiro.getNivel(),
            aventureiro.isAtivo()
        );
    }

    private AventureiroDetalheResponse toDetalhe(Aventureiro aventureiro) {
        return new AventureiroDetalheResponse(
            aventureiro.getId(),
            aventureiro.getNome(),
            aventureiro.getClasse().name(),
            aventureiro.getNivel(),
            aventureiro.isAtivo(),
            toCompanheiro(aventureiro.getCompanheiro())
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
