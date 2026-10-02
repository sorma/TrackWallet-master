package com.jm.trackwallet.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jm.trackwallet.model.Transazione;

import java.util.ArrayList;
import java.util.List;

public class TransazioneViewModelConto extends ViewModel {
    // MutableLiveData per le transazioni del conto
    private final MutableLiveData<List<Transazione>> transazioniLiveData = new MutableLiveData<>(new ArrayList<>());

    // MutableLiveData per tenere traccia dell'ID del conto
    private final MutableLiveData<String> contoIdLiveData = new MutableLiveData<>();

    // Lista delle transazioni relative al conto corrente
    private List<Transazione> transazioniConto = new ArrayList<>();

    // Getter per le transazioni del conto
    public LiveData<List<Transazione>> getTransazioniLiveData() {
        return transazioniLiveData;
    }

    // Getter per contoId
    public LiveData<String> getContoIdLiveData() {
        return contoIdLiveData;
    }

    // Imposta il conto corrente
    public void setContoId(String contoId) {
        contoIdLiveData.setValue(contoId);
        filterTransazioniByConto(contoId);
    }

    // Aggiunge una transazione al conto corrente
    public void addTransazione(Transazione transazione) {
        String contoId = contoIdLiveData.getValue();
        if (contoId != null && transazione.getIdConto().equals(contoId)) {
            List<Transazione> currentList = transazioniLiveData.getValue();
            if (currentList != null) {
                currentList.add(transazione);
                transazioniLiveData.setValue(currentList); // aggiorna la lista
            }
        }
    }

    // Imposta tutte le transazioni per il conto
    public void setTransazioni(List<Transazione> transazioni) {
        transazioniConto.clear();
        transazioniConto.addAll(transazioni);
        updateTransazioniLiveData();
    }

    // Filtra le transazioni in base al conto selezionato
    private void filterTransazioniByConto(String contoId) {
        List<Transazione> filteredList = new ArrayList<>();
        for (Transazione transazione : transazioniConto) {
            if (transazione.getIdConto().equals(contoId)) {
                filteredList.add(transazione);
            }
        }
        transazioniLiveData.setValue(filteredList);
    }

    // Cancella una transazione
    public void deleteTransazione(Transazione transazione) {
        List<Transazione> currentList = transazioniLiveData.getValue();
        if (currentList != null) {
            currentList.remove(transazione);
            transazioniLiveData.setValue(currentList);
        }
    }

    // Aggiorna LiveData con la lista delle transazioni del conto
    private void updateTransazioniLiveData() {
        transazioniLiveData.setValue(transazioniConto);
    }
}
