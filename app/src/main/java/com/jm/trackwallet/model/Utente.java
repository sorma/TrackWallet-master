package com.jm.trackwallet.model;

public class Utente {
    private String idUtente;
    private String username;
    private String mail;

    public Utente(){}
    public Utente(String idUtente, String username, String mail) {
        this.idUtente = idUtente;
        this.username = username;
        this.mail = mail;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
