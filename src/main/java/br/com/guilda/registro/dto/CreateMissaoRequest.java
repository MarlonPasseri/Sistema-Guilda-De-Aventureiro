package br.com.guilda.registro.dto;

import br.com.guilda.registro.domain.NivelPerigoMissao;
import br.com.guilda.registro.domain.StatusMissao;
import br.com.guilda.registro.validation.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class CreateMissaoRequest {

    @NotNull(message = "organizacaoId e obrigatorio")
    private Long organizacaoId;

    @NotBlank(message = "titulo e obrigatorio")
    @Size(max = 150, message = "titulo deve ter no maximo 150 caracteres")
    private String titulo;

    @NotBlank(message = "nivelPerigo e obrigatorio")
    @EnumValue(enumClass = NivelPerigoMissao.class, message = "nivelPerigo invalido")
    private String nivelPerigo;

    @NotBlank(message = "status e obrigatorio")
    @EnumValue(enumClass = StatusMissao.class, message = "status invalido")
    private String status;

    private OffsetDateTime dataInicio;

    private OffsetDateTime dataTermino;

    public Long getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(Long organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNivelPerigo() {
        return nivelPerigo;
    }

    public void setNivelPerigo(String nivelPerigo) {
        this.nivelPerigo = nivelPerigo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(OffsetDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public OffsetDateTime getDataTermino() {
        return dataTermino;
    }

    public void setDataTermino(OffsetDateTime dataTermino) {
        this.dataTermino = dataTermino;
    }
}
