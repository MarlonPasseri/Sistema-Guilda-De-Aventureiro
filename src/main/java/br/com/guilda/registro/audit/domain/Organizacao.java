package br.com.guilda.registro.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "organizacoes", schema = "audit")
public class Organizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "organizacao", fetch = FetchType.LAZY)
    private Set<Usuario> usuarios = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organizacao", fetch = FetchType.LAZY)
    private Set<Role> roles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organizacao", fetch = FetchType.LAZY)
    private Set<ApiKey> apiKeys = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organizacao", fetch = FetchType.LAZY)
    private Set<AuditEntry> auditEntries = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<ApiKey> getApiKeys() {
        return apiKeys;
    }

    public Set<AuditEntry> getAuditEntries() {
        return auditEntries;
    }
}
