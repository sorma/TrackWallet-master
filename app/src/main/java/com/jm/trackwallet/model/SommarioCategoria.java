package com.jm.trackwallet.model;

public class SommarioCategoria {
    private String idCategoria;
    private double totale;

    public SommarioCategoria(String idCategoria, double totale) {
        this.idCategoria = idCategoria;
        this.totale = totale;
    }

    public SommarioCategoria(){}

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public double getTotale() {
        return totale;
    }

    public void setTotale(double totale) {
        this.totale = totale;
    }
}
