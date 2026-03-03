package br.com.guilda.registro.repository;

import br.com.guilda.registro.domain.Aventureiro;
import br.com.guilda.registro.domain.ClasseAventureiro;
import br.com.guilda.registro.domain.Companheiro;
import br.com.guilda.registro.domain.EspecieCompanheiro;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AventureiroMemoriaRepository {

    private final List<Aventureiro> registros = new ArrayList<>();
    private final AtomicLong sequenciaId = new AtomicLong(0);

    public AventureiroMemoriaRepository() {
        carregarDadosIniciais();
    }

    public synchronized Aventureiro criar(String nome, ClasseAventureiro classe, Integer nivel) {
        Aventureiro novo = new Aventureiro();
        novo.setId(proximoId());
        novo.setNome(nome);
        novo.setClasse(classe);
        novo.setNivel(nivel);
        novo.setAtivo(true);
        novo.setCompanheiro(null);
        registros.add(novo);
        return novo;
    }

    public synchronized Optional<Aventureiro> buscarPorId(Long id) {
        return registros.stream()
            .filter(registro -> registro.getId().equals(id))
            .findFirst();
    }

    public synchronized List<Aventureiro> listarTodosOrdenadosPorId() {
        return registros.stream()
            .sorted(Comparator.comparing(Aventureiro::getId))
            .toList();
    }

    private synchronized long proximoId() {
        return sequenciaId.incrementAndGet();
    }

    private void carregarDadosIniciais() {
        ClasseAventureiro[] classes = ClasseAventureiro.values();
        EspecieCompanheiro[] especies = EspecieCompanheiro.values();

        for (int i = 1; i <= 120; i++) {
            Aventureiro aventureiro = new Aventureiro();
            aventureiro.setId(proximoId());
            aventureiro.setNome("Aventureiro " + i);
            aventureiro.setClasse(classes[(i - 1) % classes.length]);
            aventureiro.setNivel((i % 30) + 1);
            aventureiro.setAtivo(i % 9 != 0);

            if (i % 10 == 0) {
                Companheiro companheiro = new Companheiro();
                companheiro.setNome("Companheiro " + i);
                companheiro.setEspecie(especies[(i - 1) % especies.length]);
                companheiro.setLealdade((i * 7) % 101);
                aventureiro.setCompanheiro(companheiro);
            }

            registros.add(aventureiro);
        }
    }
}
