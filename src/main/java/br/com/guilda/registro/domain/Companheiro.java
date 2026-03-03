package br.com.guilda.registro.domain;

public class Companheiro {
    private String nome;
    private EspecieCompanheiro especie;
    private Integer lealdade;

    public Companheiro() {
    }

    public Companheiro(String nome, EspecieCompanheiro especie, Integer lealdade) {
        this.nome = nome;
        this.especie = especie;
        this.lealdade = lealdade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public EspecieCompanheiro getEspecie() {
        return especie;
    }

    public void setEspecie(EspecieCompanheiro especie) {
        this.especie = especie;
    }

    public Integer getLealdade() {
        return lealdade;
    }

    public void setLealdade(Integer lealdade) {
        this.lealdade = lealdade;
    }
}
