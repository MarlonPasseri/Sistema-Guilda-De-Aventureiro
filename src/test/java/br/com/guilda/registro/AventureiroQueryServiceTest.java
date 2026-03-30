package br.com.guilda.registro;

import br.com.guilda.registro.audit.domain.Organizacao;
import br.com.guilda.registro.audit.domain.Usuario;
import br.com.guilda.registro.audit.repository.OrganizacaoRepository;
import br.com.guilda.registro.audit.repository.UsuarioRepository;
import br.com.guilda.registro.domain.Aventureiro;
import br.com.guilda.registro.domain.ClasseAventureiro;
import br.com.guilda.registro.domain.Companheiro;
import br.com.guilda.registro.domain.EspecieCompanheiro;
import br.com.guilda.registro.domain.Missao;
import br.com.guilda.registro.domain.NivelPerigoMissao;
import br.com.guilda.registro.domain.PapelMissao;
import br.com.guilda.registro.domain.ParticipacaoMissao;
import br.com.guilda.registro.domain.StatusMissao;
import br.com.guilda.registro.dto.AventureiroDetalheResponse;
import br.com.guilda.registro.dto.AventureiroResumoResponse;
import br.com.guilda.registro.dto.PageResult;
import br.com.guilda.registro.repository.AventureiroRepository;
import br.com.guilda.registro.repository.MissaoRepository;
import br.com.guilda.registro.repository.ParticipacaoMissaoRepository;
import br.com.guilda.registro.service.AventureiroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AventureiroService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AventureiroQueryServiceTest {

    @Autowired
    private AventureiroService aventureiroService;

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

    @Test
    void deveListarAventureirosComFiltrosPaginacaoEOrdenacao() {
        criarAventureiro("Arthos", ClasseAventureiro.MAGO, 12, true);
        criarAventureiro("Belthor", ClasseAventureiro.MAGO, 7, true);
        criarAventureiro("Catelyn", ClasseAventureiro.GUERREIRO, 18, true);
        criarAventureiro("Doran", ClasseAventureiro.MAGO, 3, false);

        PageResult<AventureiroResumoResponse> resultado = aventureiroService.listar(
            1L, "MAGO", true, 5, 0, 10, "nivel", "desc"
        );

        assertThat(resultado.totalCount()).isEqualTo(2);
        assertThat(resultado.items()).extracting(AventureiroResumoResponse::nome)
            .containsExactly("Arthos", "Belthor");
        assertThat(resultado.items()).extracting(AventureiroResumoResponse::nivel)
            .containsExactly(12, 7);
    }

    @Test
    void deveBuscarAventureirosPorNomeParcial() {
        criarAventureiro("Arthos", ClasseAventureiro.GUERREIRO, 8, true);
        criarAventureiro("Arthemis", ClasseAventureiro.ARQUEIRO, 9, true);
        criarAventureiro("Borin", ClasseAventureiro.CLERIGO, 6, true);

        PageResult<AventureiroResumoResponse> resultado = aventureiroService.buscarPorNome(
            1L, "arth", 0, 10, "nome", "asc"
        );

        assertThat(resultado.totalCount()).isEqualTo(2);
        assertThat(resultado.items()).extracting(AventureiroResumoResponse::nome)
            .containsExactly("Arthemis", "Arthos");
    }

    @Test
    void deveRetornarPerfilCompletoComCompanheiroQuantidadeEUltimaMissao() {
        Aventureiro aventureiro = criarAventureiro("Lyra", ClasseAventureiro.LADINO, 14, true);
        aventureiro.definirCompanheiro(new Companheiro("Fenrir", EspecieCompanheiro.LOBO, 98));
        aventureiroRepository.saveAndFlush(aventureiro);

        Missao missaoAntiga = criarMissao("Ruinas do Norte", StatusMissao.CONCLUIDA, NivelPerigoMissao.MEDIO);
        Missao missaoRecente = criarMissao("Cripta Escarlate", StatusMissao.CONCLUIDA, NivelPerigoMissao.ALTO);

        criarParticipacao(aventureiro, missaoAntiga, OffsetDateTime.parse("2026-03-10T10:00:00Z"), new BigDecimal("50.00"), false);
        criarParticipacao(aventureiro, missaoRecente, OffsetDateTime.parse("2026-03-20T10:00:00Z"), new BigDecimal("120.00"), true);

        AventureiroDetalheResponse detalhe = aventureiroService.consultarPorId(1L, aventureiro.getId());

        assertThat(detalhe.nome()).isEqualTo("Lyra");
        assertThat(detalhe.companheiro()).isNotNull();
        assertThat(detalhe.companheiro().nome()).isEqualTo("Fenrir");
        assertThat(detalhe.totalParticipacoes()).isEqualTo(2);
        assertThat(detalhe.ultimaMissao()).isNotNull();
        assertThat(detalhe.ultimaMissao().titulo()).isEqualTo("Cripta Escarlate");
    }

    private Aventureiro criarAventureiro(String nome, ClasseAventureiro classe, int nivel, boolean ativo) {
        Organizacao organizacao = organizacaoRepository.findById(1L).orElseThrow();
        Usuario usuario = usuarioRepository.findById(1L).orElseThrow();

        Aventureiro aventureiro = new Aventureiro();
        aventureiro.setOrganizacao(organizacao);
        aventureiro.setUsuarioCadastro(usuario);
        aventureiro.setNome(nome);
        aventureiro.setClasse(classe);
        aventureiro.setNivel(nivel);
        aventureiro.setAtivo(ativo);
        return aventureiroRepository.saveAndFlush(aventureiro);
    }

    private Missao criarMissao(String titulo, StatusMissao status, NivelPerigoMissao nivelPerigo) {
        Organizacao organizacao = organizacaoRepository.findById(1L).orElseThrow();

        Missao missao = new Missao();
        missao.setOrganizacao(organizacao);
        missao.setTitulo(titulo);
        missao.setStatus(status);
        missao.setNivelPerigo(nivelPerigo);
        missao.setCreatedAt(OffsetDateTime.parse("2026-03-01T10:00:00Z"));
        return missaoRepository.saveAndFlush(missao);
    }

    private void criarParticipacao(
        Aventureiro aventureiro,
        Missao missao,
        OffsetDateTime dataRegistro,
        BigDecimal recompensa,
        boolean destaque
    ) {
        ParticipacaoMissao participacao = new ParticipacaoMissao();
        participacao.setPapelNaMissao(PapelMissao.EXPLORADOR);
        participacao.setDataRegistro(dataRegistro);
        participacao.setRecompensaOuro(recompensa);
        participacao.setDestaque(destaque);
        missao.adicionarParticipacao(participacao);
        aventureiro.adicionarParticipacao(participacao);
        missaoRepository.saveAndFlush(missao);
    }
}
