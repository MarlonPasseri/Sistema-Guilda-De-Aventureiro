package br.com.guilda.registro;

import br.com.guilda.registro.audit.domain.Organizacao;
import br.com.guilda.registro.audit.domain.Usuario;
import br.com.guilda.registro.audit.repository.OrganizacaoRepository;
import br.com.guilda.registro.audit.repository.UsuarioRepository;
import br.com.guilda.registro.domain.Aventureiro;
import br.com.guilda.registro.domain.ClasseAventureiro;
import br.com.guilda.registro.domain.Missao;
import br.com.guilda.registro.domain.NivelPerigoMissao;
import br.com.guilda.registro.domain.PapelMissao;
import br.com.guilda.registro.domain.ParticipacaoMissao;
import br.com.guilda.registro.domain.StatusMissao;
import br.com.guilda.registro.dto.MissaoDetalheResponse;
import br.com.guilda.registro.dto.MissaoMetricaResponse;
import br.com.guilda.registro.dto.MissaoResumoResponse;
import br.com.guilda.registro.dto.PageResult;
import br.com.guilda.registro.dto.RankingParticipacaoResponse;
import br.com.guilda.registro.repository.AventureiroRepository;
import br.com.guilda.registro.repository.MissaoRepository;
import br.com.guilda.registro.repository.ParticipacaoMissaoRepository;
import br.com.guilda.registro.service.MissaoService;
import br.com.guilda.registro.service.RelatorioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({MissaoService.class, RelatorioService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MissaoRelatorioServiceTest {

    @Autowired
    private MissaoService missaoService;

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private AventureiroRepository aventureiroRepository;

    @Autowired
    private MissaoRepository missaoRepository;

    @Autowired
    private ParticipacaoMissaoRepository participacaoRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limparDadosDoSchemaAventura() {
        jdbcTemplate.execute("delete from aventura.participacoes_missao");
        jdbcTemplate.execute("delete from aventura.companheiros");
        jdbcTemplate.execute("delete from aventura.missoes");
        jdbcTemplate.execute("delete from aventura.aventureiros");
    }

    @Test
    void deveListarMissoesComFiltros() {
        criarMissao("Patrulha Alfa", StatusMissao.CONCLUIDA, NivelPerigoMissao.ALTO, OffsetDateTime.parse("2026-03-11T10:00:00Z"));
        criarMissao("Patrulha Beta", StatusMissao.PLANEJADA, NivelPerigoMissao.ALTO, OffsetDateTime.parse("2026-03-12T10:00:00Z"));
        criarMissao("Patrulha Gama", StatusMissao.CONCLUIDA, NivelPerigoMissao.MEDIO, OffsetDateTime.parse("2026-03-13T10:00:00Z"));

        PageResult<MissaoResumoResponse> resultado = missaoService.listar(
            1L,
            "CONCLUIDA",
            "ALTO",
            OffsetDateTime.parse("2026-03-01T00:00:00Z"),
            OffsetDateTime.parse("2026-03-31T23:59:59Z"),
            0,
            10,
            "titulo",
            "asc"
        );

        assertThat(resultado.totalCount()).isEqualTo(1);
        assertThat(resultado.items()).extracting(MissaoResumoResponse::titulo).containsExactly("Patrulha Alfa");
    }

    @Test
    void deveDetalharMissaoComParticipantes() {
        Aventureiro leader = criarAventureiro("Kael", ClasseAventureiro.GUERREIRO);
        Aventureiro healer = criarAventureiro("Mira", ClasseAventureiro.CLERIGO);
        Missao missao = criarMissao("Templo da Aurora", StatusMissao.EM_ANDAMENTO, NivelPerigoMissao.EPICO, OffsetDateTime.parse("2026-03-15T10:00:00Z"));

        criarParticipacao(leader, missao, PapelMissao.LIDER, new BigDecimal("200.00"), true, OffsetDateTime.parse("2026-03-16T10:00:00Z"));
        criarParticipacao(healer, missao, PapelMissao.CURANDEIRO, new BigDecimal("180.00"), false, OffsetDateTime.parse("2026-03-16T10:05:00Z"));

        MissaoDetalheResponse detalhe = missaoService.detalhar(1L, missao.getId());

        assertThat(detalhe.titulo()).isEqualTo("Templo da Aurora");
        assertThat(detalhe.participantes()).hasSize(2);
        assertThat(detalhe.participantes()).extracting(participante -> participante.aventureiroNome())
            .containsExactlyInAnyOrder("Kael", "Mira");
    }

    @Test
    void deveGerarRankingDeParticipacao() {
        Aventureiro lyra = criarAventureiro("Lyra", ClasseAventureiro.LADINO);
        Aventureiro borin = criarAventureiro("Borin", ClasseAventureiro.GUERREIRO);

        Missao missao1 = criarMissao("Ruinas", StatusMissao.CONCLUIDA, NivelPerigoMissao.ALTO, OffsetDateTime.parse("2026-03-05T10:00:00Z"));
        Missao missao2 = criarMissao("Fortaleza", StatusMissao.CONCLUIDA, NivelPerigoMissao.EPICO, OffsetDateTime.parse("2026-03-18T10:00:00Z"));

        criarParticipacao(lyra, missao1, PapelMissao.EXPLORADOR, new BigDecimal("90.00"), true, OffsetDateTime.parse("2026-03-06T10:00:00Z"));
        criarParticipacao(lyra, missao2, PapelMissao.DANO, new BigDecimal("110.00"), false, OffsetDateTime.parse("2026-03-19T10:00:00Z"));
        criarParticipacao(borin, missao1, PapelMissao.LIDER, new BigDecimal("80.00"), false, OffsetDateTime.parse("2026-03-06T11:00:00Z"));

        List<RankingParticipacaoResponse> ranking = relatorioService.rankingParticipacao(
            1L,
            OffsetDateTime.parse("2026-03-01T00:00:00Z"),
            OffsetDateTime.parse("2026-03-31T23:59:59Z"),
            "CONCLUIDA"
        );

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).aventureiroNome()).isEqualTo("Lyra");
        assertThat(ranking.get(0).totalParticipacoes()).isEqualTo(2L);
        assertThat(ranking.get(0).totalRecompensas()).isEqualByComparingTo("200.00");
        assertThat(ranking.get(0).totalDestaques()).isEqualTo(1L);
    }

    @Test
    void deveGerarRelatorioDeMissoesComMetricasConsistentes() {
        Aventureiro lyra = criarAventureiro("Lyra", ClasseAventureiro.LADINO);
        Aventureiro mira = criarAventureiro("Mira", ClasseAventureiro.CLERIGO);

        Missao missaoComParticipantes = criarMissao("Fenda Sombria", StatusMissao.CONCLUIDA, NivelPerigoMissao.ALTO, OffsetDateTime.parse("2026-03-08T10:00:00Z"));
        Missao missaoSemParticipantes = criarMissao("Posto Avancado", StatusMissao.PLANEJADA, NivelPerigoMissao.BAIXO, OffsetDateTime.parse("2026-03-22T10:00:00Z"));

        criarParticipacao(lyra, missaoComParticipantes, PapelMissao.DANO, new BigDecimal("70.00"), false, OffsetDateTime.parse("2026-03-09T10:00:00Z"));
        criarParticipacao(mira, missaoComParticipantes, PapelMissao.SUPORTE, new BigDecimal("30.00"), true, OffsetDateTime.parse("2026-03-09T10:10:00Z"));

        List<MissaoMetricaResponse> relatorio = relatorioService.relatorioMissoes(
            1L,
            OffsetDateTime.parse("2026-03-01T00:00:00Z"),
            OffsetDateTime.parse("2026-03-31T23:59:59Z")
        );

        MissaoMetricaResponse fendaSombria = relatorio.stream()
            .filter(item -> item.titulo().equals("Fenda Sombria"))
            .findFirst()
            .orElseThrow();
        MissaoMetricaResponse postoAvancado = relatorio.stream()
            .filter(item -> item.titulo().equals("Posto Avancado"))
            .findFirst()
            .orElseThrow();

        assertThat(fendaSombria.quantidadeParticipantes()).isEqualTo(2L);
        assertThat(fendaSombria.totalRecompensas()).isEqualByComparingTo("100.00");
        assertThat(postoAvancado.quantidadeParticipantes()).isEqualTo(0L);
        assertThat(postoAvancado.totalRecompensas()).isEqualByComparingTo("0.00");
    }

    private Aventureiro criarAventureiro(String nome, ClasseAventureiro classe) {
        Organizacao organizacao = organizacaoRepository.findById(1L).orElseThrow();
        Usuario usuario = usuarioRepository.findById(1L).orElseThrow();

        Aventureiro aventureiro = new Aventureiro();
        aventureiro.setOrganizacao(organizacao);
        aventureiro.setUsuarioCadastro(usuario);
        aventureiro.setNome(nome);
        aventureiro.setClasse(classe);
        aventureiro.setNivel(10);
        aventureiro.setAtivo(true);
        return aventureiroRepository.saveAndFlush(aventureiro);
    }

    private Missao criarMissao(
        String titulo,
        StatusMissao status,
        NivelPerigoMissao nivelPerigo,
        OffsetDateTime createdAt
    ) {
        Organizacao organizacao = organizacaoRepository.findById(1L).orElseThrow();

        Missao missao = new Missao();
        missao.setOrganizacao(organizacao);
        missao.setTitulo(titulo);
        missao.setStatus(status);
        missao.setNivelPerigo(nivelPerigo);
        missao.setCreatedAt(createdAt);
        return missaoRepository.saveAndFlush(missao);
    }

    private void criarParticipacao(
        Aventureiro aventureiro,
        Missao missao,
        PapelMissao papel,
        BigDecimal recompensa,
        boolean destaque,
        OffsetDateTime dataRegistro
    ) {
        ParticipacaoMissao participacao = new ParticipacaoMissao();
        participacao.setPapelNaMissao(papel);
        participacao.setRecompensaOuro(recompensa);
        participacao.setDestaque(destaque);
        participacao.setDataRegistro(dataRegistro);
        missao.adicionarParticipacao(participacao);
        aventureiro.adicionarParticipacao(participacao);
        missaoRepository.saveAndFlush(missao);
    }
}
