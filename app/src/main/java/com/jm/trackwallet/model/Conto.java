package com.jm.trackwallet.model;

public class Conto {
    private String idConto;
    private String nome;
    private int icona;
    private String idUtente;
    private double saldo;

    public Conto()
    {}
    public Conto(String idConto, String nome,int icona, String idUtente, double saldo) {
        this.idConto = idConto;
        this.nome = nome;
        this.idUtente = idUtente;
        this.saldo = saldo;
        this.icona=icona;
    }
    @Override
    public String toString() {
        return nome; // Restituisce il nome della categoria
    }
    public int getIcona() {
        return icona;
    }

    public void setIcona(int icona) {
        this.icona = icona;
    }

    public String getIdConto() {
        return idConto;
    }

    public void setIdConto(String idConto) {
        this.idConto = idConto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Conto that = (Conto) obj;
        return idConto.equals(that.idConto); // Confronta l'ID
    }

    @Override
    public int hashCode() {
        return idConto.hashCode();
    }
}
