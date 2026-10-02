package com.jm.trackwallet.ui.welcome.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jm.trackwallet.repository.DBUtenteRepository;
import com.jm.trackwallet.repository.interfaces.UtenteRepository;
import com.jm.trackwallet.model.Utente;

public class UtenteViewModel extends ViewModel {

    private final UtenteRepository utenteRepository;
    private final MutableLiveData<Utente> utenteLiveData = new MutableLiveData<>();

    public UtenteViewModel() {
        this.utenteRepository = new DBUtenteRepository();
    }

    // Getter per osservare i dati dell'utente
    public LiveData<Utente> getUtente() {
        return utenteLiveData;
    }

    // Carica un utente tramite ID
    public void loadUtente(String idUtente) {
        utenteRepository.fetchFromServer(idUtente, utenteLiveData);
    }

    // Salva i dati dell'utente
    public void saveUtente(Utente utente) {
        utenteRepository.saveUtente(utente);
    }

    // Ascolta gli aggiornamenti in tempo reale dell'utente
    public void listenForRealTimeUpdates(String idUtente) {
        utenteRepository.listenToRealTimeUpdates(idUtente, utenteLiveData);
    }
}
