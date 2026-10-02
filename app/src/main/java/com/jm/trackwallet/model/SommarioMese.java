package com.jm.trackwallet.model;

import android.content.Context;

public class SommarioMese {
    private String nomeMese;  // Es. "Gennaio"
    private int anno;         // Es. "2025"
    private double bilancio;  // Somma delle transazioni del mese

    // Constructor
    public SommarioMese(String nomeMese, int anno, double bilancio) {
        this.nomeMese = nomeMese;
        this.anno = anno;
        this.bilancio = bilancio;
    }

    // Getter e Setter
    public String getNomeMese() {
        return nomeMese;
    }

    public int getNumMese() {
        if (nomeMese.equalsIgnoreCase("Gennaio") || nomeMese.equalsIgnoreCase("January") ||
                nomeMese.equalsIgnoreCase("Enero") || nomeMese.equalsIgnoreCase("Janvier")) {
            return 1;
        } else if (nomeMese.equalsIgnoreCase("Febbraio") || nomeMese.equalsIgnoreCase("February") ||
                nomeMese.equalsIgnoreCase("Febrero") || nomeMese.equalsIgnoreCase("Février")) {
            return 2;
        } else if (nomeMese.equalsIgnoreCase("Marzo") || nomeMese.equalsIgnoreCase("March") ||
                nomeMese.equalsIgnoreCase("Marzo") || nomeMese.equalsIgnoreCase("Mars")) {
            return 3;
        } else if (nomeMese.equalsIgnoreCase("Aprile") || nomeMese.equalsIgnoreCase("April") ||
                nomeMese.equalsIgnoreCase("Abril") || nomeMese.equalsIgnoreCase("Avril")) {
            return 4;
        } else if (nomeMese.equalsIgnoreCase("Maggio") || nomeMese.equalsIgnoreCase("May") ||
                nomeMese.equalsIgnoreCase("Mayo") || nomeMese.equalsIgnoreCase("Mai")) {
            return 5;
        } else if (nomeMese.equalsIgnoreCase("Giugno") || nomeMese.equalsIgnoreCase("June") ||
                nomeMese.equalsIgnoreCase("Junio") || nomeMese.equalsIgnoreCase("Juin")) {
            return 6;
        } else if (nomeMese.equalsIgnoreCase("Luglio") || nomeMese.equalsIgnoreCase("July") ||
                nomeMese.equalsIgnoreCase("Julio") || nomeMese.equalsIgnoreCase("Juillet")) {
            return 7;
        } else if (nomeMese.equalsIgnoreCase("Agosto") || nomeMese.equalsIgnoreCase("August") ||
                nomeMese.equalsIgnoreCase("Agosto") || nomeMese.equalsIgnoreCase("Août")) {
            return 8;
        } else if (nomeMese.equalsIgnoreCase("Settembre") || nomeMese.equalsIgnoreCase("September") ||
                nomeMese.equalsIgnoreCase("Septiembre") || nomeMese.equalsIgnoreCase("Septembre")) {
            return 9;
        } else if (nomeMese.equalsIgnoreCase("Ottobre") || nomeMese.equalsIgnoreCase("October") ||
                nomeMese.equalsIgnoreCase("Octubre") || nomeMese.equalsIgnoreCase("Octobre")) {
            return 10;
        } else if (nomeMese.equalsIgnoreCase("Novembre") || nomeMese.equalsIgnoreCase("November") ||
                nomeMese.equalsIgnoreCase("Noviembre") || nomeMese.equalsIgnoreCase("Novembre")) {
            return 11;
        } else if (nomeMese.equalsIgnoreCase("Dicembre") || nomeMese.equalsIgnoreCase("December") ||
                nomeMese.equalsIgnoreCase("Diciembre") || nomeMese.equalsIgnoreCase("Décembre")) {
            return 12;
        } else {
            throw new IllegalArgumentException("Nome del mese non valido: " + this.nomeMese);
        }
    }

    public void setNomeMese(String nomeMese) {
        this.nomeMese = nomeMese;
    }

    public int getAnno() {
        return anno;
    }

    public void setAnno(int anno) {
        this.anno = anno;
    }

    public double getBilancio() {
        return bilancio;
    }

    public void setBilancio(double bilancio) {
        this.bilancio = bilancio;
    }
}

