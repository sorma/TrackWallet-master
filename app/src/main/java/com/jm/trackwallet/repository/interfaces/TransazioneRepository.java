package com.jm.trackwallet.repository.interfaces;

import androidx.lifecycle.LiveData;

import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.repository.DBTransazioneRepository;

import java.util.List;


public interface TransazioneRepository {
    String collectionName = "transazioni";
    LiveData<List<Transazione>> getTransazioniByUtente(String idUtente);
    LiveData<List<Transazione>> getTransazioniByCategoria(String idUtente, String idCategoria);
    void getTransazioniByCategoria(String idUtente, String idCategoria, DBTransazioneRepository.TransazioniCallback callback);
    LiveData<List<Transazione>> getTransazioniByConto(String idUtente, String idConto);
    void addTransazione(Transazione transazione);
    void saveTransazione(Transazione transazione);
}
