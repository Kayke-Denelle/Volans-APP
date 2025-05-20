package com.example.volans_app.DTO;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestaoQuiz implements Parcelable {
    @SerializedName("pergunta")
    private String enunciado;
    @SerializedName("alternativas")
    private List<String> opcoes;
    private String respostaCorreta;

    // Construtor vazio
    public QuestaoQuiz() {
    }

    // Construtor com todos os campos
    public QuestaoQuiz(String enunciado, List<String> opcoes, String respostaCorreta) {
        this.enunciado = enunciado;
        this.opcoes = opcoes;
        this.respostaCorreta = respostaCorreta;
    }

    // Getters e Setters
    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public List<String> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<String> opcoes) {
        this.opcoes = opcoes;
    }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    // Implementação Parcelable
    protected QuestaoQuiz(Parcel in) {
        enunciado = in.readString();
        opcoes = in.createStringArrayList();
        respostaCorreta = in.readString();
    }

    public static final Creator<QuestaoQuiz> CREATOR = new Creator<QuestaoQuiz>() {
        @Override
        public QuestaoQuiz createFromParcel(Parcel in) {
            return new QuestaoQuiz(in);
        }

        @Override
        public QuestaoQuiz[] newArray(int size) {
            return new QuestaoQuiz[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(enunciado);
        dest.writeStringList(opcoes);
        dest.writeString(respostaCorreta);
    }

    // Método toString para debug
    @Override
    public String toString() {
        return "QuestaoQuiz{" +
                "enunciado='" + enunciado + '\'' +
                ", opcoes=" + opcoes +
                ", respostaCorreta='" + respostaCorreta + '\'' +
                '}';
    }
}
