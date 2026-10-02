package com.jm.trackwallet.repository.interfaces;

import androidx.lifecycle.LiveData;

import com.jm.trackwallet.model.Utente;

import androidx.lifecycle.MutableLiveData;

public interface UtenteRepository {
    LiveData<Utente> getUtente(String idUtente);
    void saveUtente(Utente utente);
    void fetchFromServer(String idUtente, MutableLiveData<Utente> utenteLiveData);
    void listenToRealTimeUpdates(String idUtente, MutableLiveData<Utente> utenteLiveData);
}


