package com.jm.trackwallet.repository.interfaces;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jm.trackwallet.model.Conto;

import java.util.List;

public interface ContoRepository {
    String collectionName = "conti"; // Nome della collezione in Firestore

    // Metodo per ottenere tutti i conti di un utente
    LiveData<List<Conto>> getContiByUserId(String idUtente);

    // Metodo per aggiungere un nuovo conto
    void addConto(Conto conto);

    // Metodo per eliminare un conto tramite ID
    void deleteConto(String idConto);

    // Metodo per ottenere un singolo conto tramite ID
    LiveData<Conto> getContoById(String idConto);

    void updateSaldo(String idConto, double nuovoSaldo);
    LiveData<Double> getSaldoById(String idConto);

    public void getNomeContoById(String idConto, OnCompleteListener<DocumentSnapshot> onCompleteListener);
}

