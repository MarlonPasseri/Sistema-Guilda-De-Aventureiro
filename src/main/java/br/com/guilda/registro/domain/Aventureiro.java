package br.com.guilda.registro.domain;

public class Aventureiro {
    private Long id;
    private String nome;
    private ClasseAventureiro classe;
    private Integer nivel;
    private boolean ativo;
    private Companheiro companheiro;

    public Aventureiro() {
    }

    public Aventureiro(Long id, String nome, ClasseAventureiro classe, Integer nivel, boolean ativo, Companheiro companheiro) {
        this.id = id;
        this.nome = nome;
        this.classe = classe;
        this.nivel = nivel;
        this.ativo = ativo;
        this.companheiro = companheiro;
    }

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

    public ClasseAventureiro getClasse() {
        return classe;
    }

    public void setClasse(ClasseAventureiro classe) {
        this.classe = classe;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Companheiro getCompanheiro() {
        return companheiro;
    }

    public void setCompanheiro(Companheiro companheiro) {
        this.companheiro = companheiro;
    }
}
