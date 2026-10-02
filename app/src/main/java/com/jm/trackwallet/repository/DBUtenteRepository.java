package com.jm.trackwallet.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.jm.trackwallet.model.Utente;
import com.jm.trackwallet.repository.interfaces.UtenteRepository;
import com.jm.trackwallet.manager.FirebaseManager;

public class DBUtenteRepository implements UtenteRepository {
    private FirebaseFirestore firestore;

    public DBUtenteRepository() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }

    @Override
    public LiveData<Utente> getUtente(String idUtente) {
        MutableLiveData<Utente> utenteLiveData = new MutableLiveData<>();

        // Prima prova a leggere dalla cache
        firestore.collection("utenti")
                .document(idUtente)
                .get(Source.CACHE)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Utente utente = documentSnapshot.toObject(Utente.class);
                        utenteLiveData.setValue(utente);
                    } else {
                        // Se non esiste nella cache, prendi dal server
                        fetchFromServer(idUtente, utenteLiveData);
                    }
                })
                .addOnFailureListener(e -> {
                    // Se la lettura dalla cache fallisce, prova a leggere dal server
                    fetchFromServer(idUtente, utenteLiveData);
                });

        // Ascolta per aggiornamenti in tempo reale
        listenToRealTimeUpdates(idUtente, utenteLiveData);

        return utenteLiveData;
    }

    @Override
    public void saveUtente(Utente utente) {
        firestore.collection("utenti")
                .document(utente.getIdUtente())
                .set(utente)
                .addOnSuccessListener(aVoid -> {
                    // Successo nel salvataggio
                })
                .addOnFailureListener(e -> {
                    // Gestione errore
                });
    }

    @Override
    public void fetchFromServer(String idUtente, MutableLiveData<Utente> utenteLiveData) {
        firestore.collection("utenti")
                .document(idUtente)
                .get(Source.SERVER)  // Richiesta esplicita dal server
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Utente utente = documentSnapshot.toObject(Utente.class);
                        utenteLiveData.setValue(utente);
                    } else {
                        utenteLiveData.setValue(null);  // Non trovato
                    }
                })
                .addOnFailureListener(e -> utenteLiveData.setValue(null));  // Se c'è un errore, ritorna null
    }

    @Override
    public void listenToRealTimeUpdates(String idUtente, MutableLiveData<Utente> utenteLiveData) {
        firestore.collection("utenti")
                .document(idUtente)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null) {
                        utenteLiveData.setValue(null);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Utente utente = documentSnapshot.toObject(Utente.class);
                        utenteLiveData.setValue(utente);  // Aggiorna in tempo reale
                    } else {
                        utenteLiveData.setValue(null);  // Se non esiste più
                    }
                });
    }
}
