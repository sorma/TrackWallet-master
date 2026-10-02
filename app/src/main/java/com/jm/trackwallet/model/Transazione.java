package com.jm.trackwallet.model;

import com.google.firebase.Timestamp;

public class Transazione {
    private String titolo;
    private String idTransazione;
    private String idUtente;
    private String idConto;
    private String idCategoria;
    private double importo;
    private Timestamp data;

    public Transazione(){}
    public Transazione(String idTransazione,String titolo, String idUtente, String idConto, String idCategoria, double importo, Timestamp data) {
        this.idTransazione = idTransazione;
        this.idUtente = idUtente;
        this.idConto = idConto;
        this.idCategoria = idCategoria;
        this.importo = importo;
        this.data = data;
        this.titolo=titolo;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getIdTransazione() {
        return idTransazione;
    }

    public void setIdTransazione(String idTransazione) {
        this.idTransazione = idTransazione;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getIdConto() {
        return idConto;
    }

    public void setIdConto(String idConto) {
        this.idConto = idConto;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transazione that = (Transazione) obj;
        return idTransazione.equals(that.idTransazione); // Confronta l'ID
    }

    @Override
    public int hashCode() {
        return idTransazione.hashCode();
    }
}
