package com.example.volans_app.DTO;

public class Baralho {
    private String id;
    private String nome;
    private String descricao;
    private String imagemUrl; // Nova propriedade para a URL da imagem personalizada

    // Construtor vazio
    public Baralho() {}

    // Construtor com parâmetros para nome e descrição
    public Baralho(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // Construtor completo com todos os campos
    public Baralho(String id, String nome, String descricao, String imagemUrl) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
    }

    // Getters e setters existentes
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

    // Getter e setter para a nova propriedade imagemUrl
    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    // Método toString atualizado
    @Override
    public String toString() {
        return "Baralho{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", imagemUrl='" + imagemUrl + '\'' +
                '}';
    }
}
