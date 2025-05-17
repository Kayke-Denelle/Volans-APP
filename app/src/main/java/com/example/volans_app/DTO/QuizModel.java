package com.example.volans_app.DTO;

import java.util.List;

public class QuizModel {
    private String id;
    private String baralhoId;
    private String pergunta;
    private List<String> alternativas;

    private String respostaCorreta;

    // Getters
    public String getId() {
        return id;
    }

    public String getBaralhoId() {
        return baralhoId;
    }

    public String getPergunta() {
        return pergunta;
    }

    public List<String> getAlternativas() {
        return alternativas;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setBaralhoId(String baralhoId) {
        this.baralhoId = baralhoId;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas;
    }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }
}

