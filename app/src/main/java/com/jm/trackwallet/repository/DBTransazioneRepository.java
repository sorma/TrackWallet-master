package com.jm.trackwallet.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.repository.interfaces.TransazioneRepository;
import com.jm.trackwallet.manager.FirebaseManager;

import java.util.List;

public class DBTransazioneRepository implements TransazioneRepository {
    private FirebaseFirestore firestore;

    public DBTransazioneRepository() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }

    @NonNull
    public String getNewId() {
        return firestore.collection(collectionName).document().getId();
    }

    @Override
    public LiveData<List<Transazione>> getTransazioniByUtente(String idUtente) {
        MutableLiveData<List<Transazione>> transazioniLiveData = new MutableLiveData<>();

        // Prima prova a leggere dalla cache
        firestore.collection("transazioni")
                .whereEqualTo("idUtente", idUtente)
                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                .get(Source.CACHE)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Se la cache è vuota, leggi dal server
                        firestore.collection("transazioni")
                                .whereEqualTo("idUtente", idUtente)
                                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                                .get(Source.SERVER)  // Legge dal server
                                .addOnSuccessListener(snapshot -> {
                                    List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                    transazioniLiveData.setValue(transazioni);
                                })
                                .addOnFailureListener(e -> transazioniLiveData.setValue(null));
                    } else {
                        // Se la cache non è vuota, usa i dati letti
                        List<Transazione> transazioni = querySnapshot.toObjects(Transazione.class);
                        transazioniLiveData.setValue(transazioni);
                    }
                })
                .addOnFailureListener(e -> {
                    // Se c'è errore nella lettura della cache, leggi dal server
                    firestore.collection("transazioni")
                            .whereEqualTo("idUtente", idUtente)
                            .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                            .get(Source.SERVER)  // Legge dal server
                            .addOnSuccessListener(snapshot -> {
                                List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                transazioniLiveData.setValue(transazioni);
                            })
                            .addOnFailureListener(ex -> transazioniLiveData.setValue(null));
                });

        // Ascolto aggiornamenti in tempo reale
        listenToRealTimeUpdates(idUtente, transazioniLiveData);

        return transazioniLiveData;
    }


    @Override
    public LiveData<List<Transazione>> getTransazioniByConto(String idUtente, String idConto) {
        MutableLiveData<List<Transazione>> transazioniLiveData = new MutableLiveData<>();

        // Prima otteniamo i dati dalla cache
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("idConto", idConto)
                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                .get(Source.CACHE)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Se la cache è vuota, leggi dal server
                        firestore.collection(collectionName)
                                .whereEqualTo("idUtente", idUtente)
                                .whereEqualTo("idConto", idConto)
                                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                                .get(Source.SERVER)  // Legge dal server
                                .addOnSuccessListener(snapshot -> {
                                    List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                    transazioniLiveData.setValue(transazioni);
                                })
                                .addOnFailureListener(e -> transazioniLiveData.setValue(null));
                    } else {
                        // Se la cache non è vuota, usa i dati letti
                        List<Transazione> transazioni = querySnapshot.toObjects(Transazione.class);
                        transazioniLiveData.setValue(transazioni);
                    }
                })
                .addOnFailureListener(e -> {
                    // Se errore nella lettura della cache, cerca nel server
                    firestore.collection(collectionName)
                            .whereEqualTo("idUtente", idUtente)
                            .whereEqualTo("idConto", idConto)
                            .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                            .get(Source.SERVER)  // Forza lettura dal server
                            .addOnSuccessListener(snapshot -> {
                                List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                transazioniLiveData.setValue(transazioni);
                            })
                            .addOnFailureListener(ex -> transazioniLiveData.setValue(null));
                });

        // Aggiungiamo il listener per gli aggiornamenti in tempo reale
        listenToRealTimeUpdatesConto(idUtente, idConto, transazioniLiveData);

        return transazioniLiveData;
    }


    @Override
    public LiveData<List<Transazione>> getTransazioniByCategoria(String idUtente, String idCategoria) {
        MutableLiveData<List<Transazione>> transazioniLiveData = new MutableLiveData<>();

        // Prima otteniamo i dati dalla cache
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("idCategoria", idCategoria)
                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                .get(Source.CACHE)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Se la cache è vuota, leggi dal server
                        firestore.collection(collectionName)
                                .whereEqualTo("idUtente", idUtente)
                                .whereEqualTo("idCategoria", idCategoria)
                                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                                .get(Source.SERVER)  // Forza lettura dal server
                                .addOnSuccessListener(snapshot -> {
                                    List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                    transazioniLiveData.setValue(transazioni);
                                })
                                .addOnFailureListener(e -> transazioniLiveData.setValue(null));
                    } else {
                        // Se la cache non è vuota, usa i dati letti
                        List<Transazione> transazioni = querySnapshot.toObjects(Transazione.class);
                        transazioniLiveData.setValue(transazioni);
                    }
                })
                .addOnFailureListener(e -> {
                    // Se errore nella lettura della cache, cerca nel server
                    firestore.collection(collectionName)
                            .whereEqualTo("idUtente", idUtente)
                            .whereEqualTo("idCategoria", idCategoria)
                            .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                            .get(Source.SERVER)  // Forza lettura dal server
                            .addOnSuccessListener(snapshot -> {
                                List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                transazioniLiveData.setValue(transazioni);
                            })
                            .addOnFailureListener(ex -> transazioniLiveData.setValue(null));
                });

        // Aggiungiamo il listener per gli aggiornamenti in tempo reale
        listenToRealTimeUpdatesCategoria(idUtente, idCategoria, transazioniLiveData);

        return transazioniLiveData;
    }
    @Override
    public void getTransazioniByCategoria(String idUtente, String idCategoria, TransazioniCallback callback) {
        // Prima otteniamo i dati dalla cache
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("idCategoria", idCategoria)
                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                .get(Source.CACHE)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Se la cache è vuota, leggi dal server
                        firestore.collection(collectionName)
                                .whereEqualTo("idUtente", idUtente)
                                .whereEqualTo("idCategoria", idCategoria)
                                .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                                .get(Source.SERVER)  // Forza lettura dal server
                                .addOnSuccessListener(snapshot -> {
                                    List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                    // Passa le transazioni al callback
                                    callback.onSuccess(transazioni);
                                })
                                .addOnFailureListener(e -> callback.onFailure(e));
                    } else {
                        // Se la cache non è vuota, usa i dati letti
                        List<Transazione> transazioni = querySnapshot.toObjects(Transazione.class);
                        // Passa le transazioni al callback
                        callback.onSuccess(transazioni);
                    }
                })
                .addOnFailureListener(e -> {
                    // Se errore nella lettura della cache, cerca nel server
                    firestore.collection(collectionName)
                            .whereEqualTo("idUtente", idUtente)
                            .whereEqualTo("idCategoria", idCategoria)
                            .orderBy("data", Query.Direction.DESCENDING)  // Ordina per data decrescente
                            .get(Source.SERVER)  // Forza lettura dal server
                            .addOnSuccessListener(snapshot -> {
                                List<Transazione> transazioni = snapshot.toObjects(Transazione.class);
                                // Passa le transazioni al callback
                                callback.onSuccess(transazioni);
                            })
                            .addOnFailureListener(ex -> callback.onFailure(ex));
                });
    }

    // Interfaccia per il callback
    public interface TransazioniCallback {
        void onSuccess(List<Transazione> transazioni);
        void onFailure(Exception e);
    }


    private void listenToRealTimeUpdatesConto(String idUtente, String idConto, MutableLiveData<List<Transazione>> transazioniLiveData) {
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("idConto", idConto)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) {
                        transazioniLiveData.setValue(null);
                        return;
                    }

                    List<Transazione> transazioni = querySnapshot.toObjects(Transazione.class);
                    transazioniLiveData.setValue(transazioni);
                });
    }

    private void listenToRealTimeUpdatesCategoria(String idUtente, String idCategoria, MutableLiveData<List<Transazione>> transazioniLiveData) {
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("idCategoria", idCategoria)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) {
                        transazioniLiveData.setValue(null);
                        return;
                    }

                    List<Transazione> transazioni = querySnapshot.toObjects(Transazione.class);
                    transazioniLiveData.setValue(transazioni);
                });
    }

    private void listenToRealTimeUpdates(String idUtente, MutableLiveData<List<Transazione>> transazioniLiveData) {
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) {
                        transazioniLiveData.setValue(null);
                        return;
                    }

                    List<Transazione> transazioni = querySnapshot.toObjects(Transazione.class);
                    transazioniLiveData.setValue(transazioni);
                });
    }

    @Override
    public void addTransazione(Transazione transazione) {
        String idTransazione = getNewId();
        transazione.setIdTransazione(idTransazione);
        firestore.collection(collectionName)
                .document(idTransazione)
                .set(transazione)
                .addOnSuccessListener(aVoid -> {
                    // Successo nell'aggiungere la transazione
                })
                .addOnFailureListener(e -> {
                    // Gestione errore
                });
    }

    @Override
    public void saveTransazione(Transazione transazione) {
        firestore.collection(collectionName)
                .document(transazione.getIdTransazione())
                .set(transazione)
                .addOnSuccessListener(aVoid -> {
                    // Successo nel salvataggio
                })
                .addOnFailureListener(e -> {
                    // Gestione errore
                });
    }

    public void deleteTransazione(Transazione transazione) {
        firestore.collection(collectionName)
                .document(transazione.getIdTransazione())
                .delete()
                .addOnSuccessListener(aVoid -> {
                   //eliminata
                })
                .addOnFailureListener(e -> {
                    // Gestire errore di cancellazione
                });
    }

    public void deleteTransazioniByConto(String idConto) {
        firestore.collection(collectionName)
                .whereEqualTo("idConto", idConto)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        WriteBatch batch = firestore.batch();

                        // Aggiunge ogni transazione al batch per l'eliminazione
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            batch.delete(document.getReference());
                        }

                        // Esegue il batch per eliminare tutte le transazioni insieme
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Tutte le transazioni del conto " + idConto + " sono state eliminate con successo.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Errore nell'eliminazione delle transazioni del conto", e);
                                });
                    } else {
                        Log.d("Firestore", "Nessuna transazione trovata per il conto " + idConto);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Errore nel recuperare le transazioni del conto", e);
                });
    }
    public void deleteTransazioniByCategoria(String idCategoria) {
        firestore.collection(collectionName)
                .whereEqualTo("idCategoria", idCategoria)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        WriteBatch batch = firestore.batch();

                        // Aggiunge ogni transazione al batch per l'eliminazione
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            batch.delete(document.getReference());
                        }

                        // Esegue il batch per eliminare tutte le transazioni insieme
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Tutte le transazioni del conto " + idCategoria + " sono state eliminate con successo.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Errore nell'eliminazione delle transazioni del conto", e);
                                });
                    } else {
                        Log.d("Firestore", "Nessuna transazione trovata per il conto " + idCategoria);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Errore nel recuperare le transazioni del conto", e);
                });
    }

}


