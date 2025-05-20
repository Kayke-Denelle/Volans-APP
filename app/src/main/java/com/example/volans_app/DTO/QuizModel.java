package com.example.volans_app.DTO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class QuizModel implements Parcelable {
    private String id;
    private String baralhoId;
    private List<QuestaoQuiz> perguntas;  // Alterado para lista de QuestaoQuiz

    // Construtor vazio
    public QuizModel() {
    }

    // Construtor com todos os campos
    public QuizModel(String id, String baralhoId, List<QuestaoQuiz> perguntas) {
        this.id = id;
        this.baralhoId = baralhoId;
        this.perguntas = perguntas;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaralhoId() {
        return baralhoId;
    }

    public void setBaralhoId(String baralhoId) {
        this.baralhoId = baralhoId;
    }

    public List<QuestaoQuiz> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<QuestaoQuiz> perguntas) {
        this.perguntas = perguntas;
    }

    // Implementação Parcelable
    protected QuizModel(Parcel in) {
        id = in.readString();
        baralhoId = in.readString();
        perguntas = in.createTypedArrayList(QuestaoQuiz.CREATOR);
    }

    public static final Creator<QuizModel> CREATOR = new Creator<QuizModel>() {
        @Override
        public QuizModel createFromParcel(Parcel in) {
            return new QuizModel(in);
        }

        @Override
        public QuizModel[] newArray(int size) {
            return new QuizModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(baralhoId);
        dest.writeTypedList(perguntas);
    }

    @Override
    public String toString() {
        return "QuizModel{" +
                "id='" + id + '\'' +
                ", baralhoId='" + baralhoId + '\'' +
                ", perguntas=" + perguntas +
                '}';
    }
}