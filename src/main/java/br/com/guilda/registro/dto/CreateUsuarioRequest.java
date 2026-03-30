package br.com.guilda.registro.dto;

import br.com.guilda.registro.audit.domain.UsuarioStatus;
import br.com.guilda.registro.validation.EnumValue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class CreateUsuarioRequest {

    @NotNull(message = "organizacaoId e obrigatorio")
    private Long organizacaoId;

    @NotBlank(message = "nome e obrigatorio")
    private String nome;

    @NotBlank(message = "email e obrigatorio")
    @Email(message = "email invalido")
    private String email;

    @NotBlank(message = "senhaHash e obrigatorio")
    private String senhaHash;

    @NotBlank(message = "status e obrigatorio")
    @EnumValue(enumClass = UsuarioStatus.class, message = "status invalido")
    private String status;

    @NotEmpty(message = "roleIds deve possuir ao menos um item")
    private Set<Long> roleIds;

    public Long getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(Long organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
