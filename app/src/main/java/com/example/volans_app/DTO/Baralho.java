package com.example.volans_app.DTO;

public class Baralho {
    private String id;
    private String nome;
    private String descricao;

    public Baralho() {}

    // Construtor com parâmetros para nome e descrição
    public Baralho(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters e setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
