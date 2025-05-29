package com.example.volans_app.model;

public class ScheduleItem {
    private String subject;
    private String professor;
    private String mode;
    private String time;
    private String day;

    public ScheduleItem(String subject, String professor, String mode, String time, String day) {
        this.subject = subject;
        this.professor = professor;
        this.mode = mode;
        this.time = time;
        this.day = day;
    }

    // Getters
    public String getSubject() { return subject; }
    public String getProfessor() { return professor; }
    public String getMode() { return mode; }
    public String getTime() { return time; }
    public String getDay() { return day; }

    // Setters
    public void setSubject(String subject) { this.subject = subject; }
    public void setProfessor(String professor) { this.professor = professor; }
    public void setMode(String mode) { this.mode = mode; }
    public void setTime(String time) { this.time = time; }
    public void setDay(String day) { this.day = day; }
}