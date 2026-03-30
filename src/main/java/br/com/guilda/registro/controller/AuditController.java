package br.com.guilda.registro.controller;

import br.com.guilda.registro.dto.CreateUsuarioRequest;
import br.com.guilda.registro.dto.RoleAuditResponse;
import br.com.guilda.registro.dto.UsuarioAuditResponse;
import br.com.guilda.registro.service.AuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioAuditResponse> listarUsuarios(
        @RequestParam @Positive(message = "organizacaoId deve ser maior que 0") Long organizacaoId
    ) {
        return auditService.listarUsuariosComRoles(organizacaoId);
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioAuditResponse consultarUsuario(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id
    ) {
        return auditService.consultarUsuario(id);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioAuditResponse> criarUsuario(
        @Valid @RequestBody CreateUsuarioRequest request,
        UriComponentsBuilder uriBuilder
    ) {
        UsuarioAuditResponse criado = auditService.criarUsuario(request);
        URI location = uriBuilder.path("/audit/usuarios/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/roles")
    public List<RoleAuditResponse> listarRoles(
        @RequestParam @Positive(message = "organizacaoId deve ser maior que 0") Long organizacaoId
    ) {
        return auditService.listarRolesComPermissions(organizacaoId);
    }

    @GetMapping("/roles/{id}")
    public RoleAuditResponse consultarRole(
        @PathVariable @Positive(message = "id deve ser maior que 0") Long id
    ) {
        return auditService.consultarRole(id);
    }
}
