package com.example.volans_app.DTO;

public class Tarefa {
    private String id;
    private String titulo;
    private String diaSemana;
    private String horario;
    private String dataLimite;
    private String professor;
    private String modalidade;
    private String status;

    public Tarefa() {
        // Construtor vazio necessário
    }

    // Construtor completo para criar novas tarefas
    public Tarefa(String titulo, String diaSemana, String horario, String dataLimite, String modalidade, String professor) {
        this.titulo = titulo;
        this.diaSemana = diaSemana;
        this.horario = horario;
        this.dataLimite = dataLimite;
        this.modalidade = modalidade;
        this.professor = professor;
        this.status = "Pendente"; // Status padrão para novas tarefas
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(String dataLimite) {
        this.dataLimite = dataLimite;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isConcluida() {
        return "Concluída".equals(status);
    }
}
