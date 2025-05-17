package com.example.volans_app.DTO;

public class ResultadoQuiz {
    private boolean acertou;
    private String respostaCorreta;
    private String explicacao;

    public boolean isAcertou() { return acertou; }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public void setExplicacao(String explicacao) {
        this.explicacao = explicacao;
    }

    public void setAcertou(boolean acertou) {
        this.acertou = acertou;
    }
    // Getters e Setters
}

