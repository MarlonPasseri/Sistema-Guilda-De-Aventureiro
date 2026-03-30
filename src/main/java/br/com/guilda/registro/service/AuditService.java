package br.com.guilda.registro.service;

import br.com.guilda.registro.audit.domain.Organizacao;
import br.com.guilda.registro.audit.domain.Permission;
import br.com.guilda.registro.audit.domain.Role;
import br.com.guilda.registro.audit.domain.Usuario;
import br.com.guilda.registro.audit.domain.UsuarioStatus;
import br.com.guilda.registro.audit.repository.OrganizacaoRepository;
import br.com.guilda.registro.audit.repository.RoleRepository;
import br.com.guilda.registro.audit.repository.UsuarioRepository;
import br.com.guilda.registro.dto.CreateUsuarioRequest;
import br.com.guilda.registro.dto.RoleAuditResponse;
import br.com.guilda.registro.dto.UsuarioAuditResponse;
import br.com.guilda.registro.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
@Transactional
public class AuditService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final OrganizacaoRepository organizacaoRepository;

    public AuditService(
        UsuarioRepository usuarioRepository,
        RoleRepository roleRepository,
        OrganizacaoRepository organizacaoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAuditResponse> listarUsuariosComRoles(Long organizacaoId) {
        return usuarioRepository.findAllByOrganizacao_IdOrderByNomeAsc(organizacaoId).stream()
            .map(this::toUsuarioResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioAuditResponse consultarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findDetailedById(id)
            .orElseThrow(() -> ApiException.notFound("Usuario com id " + id + " nao encontrado"));
        return toUsuarioResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<RoleAuditResponse> listarRolesComPermissions(Long organizacaoId) {
        return roleRepository.findAllByOrganizacao_IdOrderByNomeAsc(organizacaoId).stream()
            .map(this::toRoleResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public RoleAuditResponse consultarRole(Long id) {
        Role role = roleRepository.findDetailedById(id)
            .orElseThrow(() -> ApiException.notFound("Role com id " + id + " nao encontrada"));
        return toRoleResponse(role);
    }

    public UsuarioAuditResponse criarUsuario(CreateUsuarioRequest request) {
        Organizacao organizacao = organizacaoRepository.findById(request.getOrganizacaoId())
            .orElseThrow(() -> ApiException.notFound("Organizacao com id " + request.getOrganizacaoId() + " nao encontrada"));

        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        if (roles.size() != request.getRoleIds().size()) {
            throw ApiException.notFound("Uma ou mais roles nao foram encontradas");
        }
        if (roles.stream().anyMatch(role -> !role.getOrganizacao().getId().equals(organizacao.getId()))) {
            throw ApiException.invalid(List.of("Todas as roles devem pertencer a organizacao informada"));
        }

        Usuario usuario = new Usuario();
        usuario.setOrganizacao(organizacao);
        usuario.setNome(request.getNome().trim());
        usuario.setEmail(request.getEmail().trim().toLowerCase());
        usuario.setSenhaHash(request.getSenhaHash().trim());
        usuario.setStatus(UsuarioStatus.valueOf(request.getStatus().trim()));
        roles.stream()
            .sorted(Comparator.comparing(Role::getNome))
            .forEach(usuario::addRole);

        Usuario salvo = usuarioRepository.save(usuario);
        return consultarUsuario(salvo.getId());
    }

    private UsuarioAuditResponse toUsuarioResponse(Usuario usuario) {
        Set<String> roles = new TreeSet<>();
        usuario.getRoles().stream()
            .map(Role::getNome)
            .forEach(roles::add);

        return new UsuarioAuditResponse(
            usuario.getId(),
            usuario.getOrganizacao().getId(),
            usuario.getOrganizacao().getNome(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getStatus().name(),
            roles
        );
    }

    private RoleAuditResponse toRoleResponse(Role role) {
        Set<String> permissions = new TreeSet<>();
        role.getPermissions().stream()
            .map(Permission::getCode)
            .forEach(permissions::add);

        return new RoleAuditResponse(
            role.getId(),
            role.getOrganizacao().getId(),
            role.getNome(),
            role.getDescricao(),
            permissions
        );
    }
}
