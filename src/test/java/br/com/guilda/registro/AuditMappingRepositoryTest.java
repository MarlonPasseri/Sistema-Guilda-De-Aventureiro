package br.com.guilda.registro;

import br.com.guilda.registro.audit.domain.Organizacao;
import br.com.guilda.registro.audit.domain.Role;
import br.com.guilda.registro.audit.domain.Usuario;
import br.com.guilda.registro.audit.domain.UsuarioStatus;
import br.com.guilda.registro.audit.repository.OrganizacaoRepository;
import br.com.guilda.registro.audit.repository.RoleRepository;
import br.com.guilda.registro.audit.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditMappingRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Test
    void deveCarregarUsuarioComRoles() {
        Usuario usuario = usuarioRepository.findDetailedById(1L).orElseThrow();

        assertThat(usuario.getNome()).isEqualTo("Root Norte");
        assertThat(usuario.getOrganizacao().getNome()).isEqualTo("Guilda do Norte");
        assertThat(usuario.getRoles()).extracting(Role::getNome).containsExactly("ADMIN");
        assertThat(usuario.getUserRoles()).hasSize(1);
        assertThat(usuario.getUserRoles().iterator().next().getGrantedAt()).isNotNull();
    }

    @Test
    void deveCarregarRoleComPermissions() {
        Role role = roleRepository.findDetailedById(1L).orElseThrow();

        assertThat(role.getNome()).isEqualTo("ADMIN");
        assertThat(role.getOrganizacao().getNome()).isEqualTo("Guilda do Norte");
        assertThat(role.getPermissions())
            .extracting(permission -> permission.getCode())
            .contains(
                "USUARIO_READ",
                "USUARIO_WRITE",
                "AUDIT_READ",
                "AVENTUREIRO_CREATE",
                "AVENTUREIRO_UPDATE",
                "AVENTUREIRO_DELETE"
            );
    }

    @Test
    void devePersistirNovoUsuarioAssociadoAOrganizacaoExistente() {
        Organizacao organizacao = organizacaoRepository.findById(1L).orElseThrow();
        Role admin = roleRepository.findById(1L).orElseThrow();

        Usuario usuario = new Usuario();
        usuario.setOrganizacao(organizacao);
        usuario.setNome("Nova Operadora");
        usuario.setEmail("nova.operadora@guildanorte.com");
        usuario.setSenhaHash("hash_novo_usuario");
        usuario.setStatus(UsuarioStatus.ATIVO);
        usuario.addRole(admin);

        Usuario salvo = usuarioRepository.saveAndFlush(usuario);

        Usuario carregado = usuarioRepository.findDetailedById(salvo.getId()).orElseThrow();
        assertThat(carregado.getOrganizacao().getId()).isEqualTo(organizacao.getId());
        assertThat(carregado.getRoles()).extracting(Role::getNome).containsExactly("ADMIN");
    }
}
