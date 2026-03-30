package br.com.guilda.registro.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String mensagem;
    private final List<String> detalhes;

    public ApiException(HttpStatus status, String mensagem, List<String> detalhes) {
        super(mensagem);
        this.status = status;
        this.mensagem = mensagem;
        this.detalhes = List.copyOf(detalhes);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public List<String> getDetalhes() {
        return detalhes;
    }

    public static ApiException invalid(List<String> detalhes) {
        return new ApiException(HttpStatus.BAD_REQUEST, "Solicitacao invalida", detalhes);
    }

    public static ApiException notFound(String detalhe) {
        return new ApiException(HttpStatus.NOT_FOUND, "Recurso nao encontrado", List.of(detalhe));
    }

    public static ApiException conflict(String detalhe) {
        return new ApiException(HttpStatus.CONFLICT, "Conflito de dados", List.of(detalhe));
    }
}
