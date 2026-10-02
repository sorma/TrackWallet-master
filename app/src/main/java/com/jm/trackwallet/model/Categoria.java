package com.jm.trackwallet.model;

public class Categoria {
    private String idCategoria;
    private String nome;
    private boolean tipo;
    //false spese
    //true entrate
    private int icona;
    private int colore;
    private String idUtente;

    public  Categoria(){}
    public Categoria(String idCategoria, String nome, int icona, int colore, boolean tipo, String idUtente) {
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.icona = icona;
        this.colore = colore;
        this.idUtente = idUtente;
        this.tipo = tipo;
    }
    @Override
    public String toString() {
        return nome; // Restituisce il nome della categoria
    }
    public boolean getTipo() {
        return tipo;
    }

    public void setTipo(boolean tipo) {
        this.tipo = tipo;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIcona() {
        return icona;
    }

    public void setIcona(int icona) {
        this.icona = icona;
    }

    public int getColore() {
        return colore;
    }

    public void setColore(int colore) {
        this.colore = colore;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Categoria that = (Categoria) obj;
        return idCategoria.equals(that.idCategoria); // Confronta l'ID
    }

    @Override
    public int hashCode() {
        return idCategoria.hashCode();
    }
}
