package com.jm.trackwallet.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jm.trackwallet.model.Transazione;

import java.util.ArrayList;
import java.util.List;

public class TransazioneViewModelCategoria extends ViewModel {
    // MutableLiveData per le transazioni generiche
    private final MutableLiveData<List<Transazione>> transazioniLiveData = new MutableLiveData<>(new ArrayList<>());

    // MutableLiveData per tenere traccia del categoriaId
    private final MutableLiveData<String> categoriaIdLiveData = new MutableLiveData<>();

    // La lista delle transazioni relative alla categoria corrente
    private List<Transazione> transazioniCategoria = new ArrayList<>();

    // Getter per le transazioni
    public LiveData<List<Transazione>> getTransazioniLiveData() {
        return transazioniLiveData;
    }

    // Getter per categoriaId
    public LiveData<String> getCategoriaIdLiveData() {
        return categoriaIdLiveData;
    }

    // Imposta la categoria corrente
    public void setCategoriaId(String categoriaId) {
        categoriaIdLiveData.setValue(categoriaId);
        // Filtra le transazioni in base alla categoria
        filterTransazioniByCategoria(categoriaId);
    }

    // Funzione per aggiungere una transazione
    public void addTransazione(Transazione transazione) {
        String categoriaId = categoriaIdLiveData.getValue();
        if (categoriaId != null && transazione.getIdCategoria().equals(categoriaId)) {
            List<Transazione> currentList = transazioniLiveData.getValue();
            if (currentList != null) {
                currentList.add(transazione);
                transazioniCategoria.clear();
                transazioniCategoria.addAll(currentList);
                updateTransazioniLiveData();
            }
        }
    }

    // Funzione per settare tutte le transazioni
    public void setTransazioni(List<Transazione> transazioni) {
        transazioniCategoria.clear();
        transazioniCategoria.addAll(transazioni);
        updateTransazioniLiveData();
    }

    // Funzione per filtrare le transazioni in base alla categoria
    private void filterTransazioniByCategoria(String categoriaId) {
        List<Transazione> filteredList = new ArrayList<>();
        for (Transazione transazione : transazioniCategoria) {
            if (transazione.getIdCategoria().equals(categoriaId)) {
                filteredList.add(transazione);
            }
        }
        transazioniLiveData.setValue(filteredList);
    }
    public void deleteTransazione(Transazione transazione) {
        // Ottieni la lista corrente di transazioni
        String categoriaId = categoriaIdLiveData.getValue();
        List<Transazione> currentList = transazioniLiveData.getValue();
        if (categoriaId != null && transazione.getIdCategoria().equals(categoriaId)) {
            if (currentList != null) {
                currentList.remove(transazione);
                transazioniCategoria.clear();
                transazioniCategoria.addAll(currentList);
                updateTransazioniLiveData();
            }
        }

    }


    // Funzione per aggiornare la LiveData delle transazioni
    private void updateTransazioniLiveData() {
        transazioniLiveData.setValue(transazioniCategoria);
    }
}
