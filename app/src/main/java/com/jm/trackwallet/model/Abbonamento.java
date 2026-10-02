package com.jm.trackwallet.model;

import com.google.firebase.Timestamp;

public class Abbonamento {
    private String idAbbonamento;
    private String idUtente;
    private String nome;
    private String conto;
    private String categoria;
    private double importo;
    private Timestamp dataInizio;
    private Timestamp dataFine;

    public Abbonamento() {
    }

    public Abbonamento(String idAbbonamento, String idUtente, String nome, String conto, String categoria, double importo, Timestamp dataInizio, Timestamp dataFine) {
        this.idAbbonamento = idAbbonamento;
        this.idUtente = idUtente;
        this.nome = nome;
        this.conto = conto;
        this.categoria = categoria;
        this.importo = importo;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    public String getIdAbbonamento() {
        return idAbbonamento;
    }

    public void setIdAbbonamento(String idAbbonamento) {
        this.idAbbonamento = idAbbonamento;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getConto() {
        return conto;
    }

    public void setConto(String conto) {
        this.conto = conto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public Timestamp getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(Timestamp dataInizio) {
        this.dataInizio = dataInizio;
    }

    public Timestamp getDataFine() {
        return dataFine;
    }

    public void setDataFine(Timestamp dataFine) {
        this.dataFine = dataFine;
    }

    @Override
    public String toString() {
        return nome;
    }
}
