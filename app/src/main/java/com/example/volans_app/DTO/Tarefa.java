package com.example.volans_app.DTO;

public class Tarefa {
    private String titulo;
    private String diaSemana;
    private String horario;
    private String dataLimite;
    private String modalidade;
    private String professor;
    private boolean concluida;

    // Construtor vazio para criação de novas tarefas
    public Tarefa() {
        this.concluida = false;
    }

    // Construtor completo
    public Tarefa(String titulo, String diaSemana, String horario, String dataLimite, String modalidade, String professor) {
        this.titulo = titulo;
        this.diaSemana = diaSemana;
        this.horario = horario;
        this.dataLimite = dataLimite;
        this.modalidade = modalidade;
        this.professor = professor;
        this.concluida = false;
    }

    // Getters
    public String getTitulo() {
        return titulo;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public String getHorario() {
        return horario;
    }

    public String getDataLimite() {
        return dataLimite;
    }

    public String getModalidade() {
        return modalidade;
    }

    public String getProfessor() {
        return professor;
    }

    public boolean isConcluida() {
        return concluida;
    }

    // Setters
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public void setDataLimite(String dataLimite) {
        this.dataLimite = dataLimite;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }
}