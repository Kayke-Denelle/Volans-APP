package com.example.volans_app.DTO;

public class Flashcard {

    private String pergunta;
    private String resposta;
    private String baralhoId;

    private boolean mostrandoResposta = false;

    public boolean isMostrandoResposta() {
        return mostrandoResposta;
    }

    public void setMostrandoResposta(boolean mostrandoResposta) {
        this.mostrandoResposta = mostrandoResposta;
    }
    public Flashcard() {}
    public Flashcard( String pergunta, String resposta, String baralhoId) {
        this.pergunta = pergunta;
        this.resposta = resposta;
        this.baralhoId = baralhoId;
    }
    // Getters e setters

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public String getBaralhoId() {
        return baralhoId;
    }

    public void setBaralhoId(String baralhoId) {
        this.baralhoId = baralhoId;
    }
}
