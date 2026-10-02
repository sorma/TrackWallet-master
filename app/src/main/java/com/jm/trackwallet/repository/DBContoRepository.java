package com.jm.trackwallet.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.jm.trackwallet.model.Conto;
import com.jm.trackwallet.repository.interfaces.ContoRepository;
import com.jm.trackwallet.manager.FirebaseManager;

import java.util.List;

public class DBContoRepository implements ContoRepository {
    private final FirebaseFirestore firestore;

    public DBContoRepository() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }

    @NonNull
    public String getNewId() {
        return firestore.collection(collectionName).document().getId();
    }

    @Override
    public void addConto(Conto conto) {
        firestore.collection(collectionName)
                .document(conto.getIdConto())
                .set(conto)
                .addOnSuccessListener(aVoid -> {
                    // Conto aggiunto con successo
                })
                .addOnFailureListener(e -> {
                    // Gestione errore
                });
    }

    @Override
    public LiveData<List<Conto>> getContiByUserId(String idUtente) {
        MutableLiveData<List<Conto>> contiLiveData = new MutableLiveData<>();

        // Prima prova a leggere dalla cache
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .orderBy("nome", Query.Direction.ASCENDING)
                .get(Source.CACHE)  // Legge dalla cache
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Se la cache è vuota, leggi dal server
                        firestore.collection(collectionName)
                                .whereEqualTo("idUtente", idUtente)
                                .orderBy("nome", Query.Direction.ASCENDING)
                                .get(Source.SERVER)  // Legge dal server
                                .addOnSuccessListener(snapshot -> {
                                    List<Conto> conti = snapshot.toObjects(Conto.class);
                                    contiLiveData.setValue(conti);
                                })
                                .addOnFailureListener(e -> contiLiveData.setValue(null));
                    } else {
                        // Se la cache contiene dati, usali
                        List<Conto> conti = querySnapshot.toObjects(Conto.class);
                        contiLiveData.setValue(conti);
                    }
                })
                .addOnFailureListener(e -> {
                    // Se errore nella lettura della cache, prova a leggere dal server
                    firestore.collection(collectionName)
                            .whereEqualTo("idUtente", idUtente)
                            .orderBy("nome", Query.Direction.ASCENDING)
                            .get(Source.SERVER)
                            .addOnSuccessListener(snapshot -> {
                                List<Conto> conti = snapshot.toObjects(Conto.class);
                                contiLiveData.setValue(conti);
                            })
                            .addOnFailureListener(ex -> contiLiveData.setValue(null));
                });

        // Aggiungi listener per aggiornamenti in tempo reale
        listenToRealTimeUpdates(idUtente, contiLiveData);
        return contiLiveData;
    }

    @Override
    public void deleteConto(String idConto) {
        firestore.collection(collectionName)
                .document(idConto)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Conto eliminato con successo
                })
                .addOnFailureListener(e -> {
                    // Gestione errore
                });
    }

    @Override
    public LiveData<Conto> getContoById(String idConto) {
        MutableLiveData<Conto> contoLiveData = new MutableLiveData<>();

        // Leggi prima dalla cache
        firestore.collection(collectionName)
                .document(idConto)
                .get(Source.CACHE)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Conto conto = documentSnapshot.toObject(Conto.class);
                        contoLiveData.setValue(conto);
                    } else {
                        contoLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    contoLiveData.setValue(null);
                });

        // Aggiungi il listener per aggiornamenti in tempo reale
        listenToRealTimeUpdatesForConto(idConto, contoLiveData);
        return contoLiveData;
    }

    private void listenToRealTimeUpdates(String idUtente, MutableLiveData<List<Conto>> contiLiveData) {
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .orderBy("nome", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) {
                        contiLiveData.setValue(null);
                        return;
                    }

                    List<Conto> conti = querySnapshot.toObjects(Conto.class);
                    contiLiveData.setValue(conti);
                });
    }

    private void listenToRealTimeUpdatesForConto(String idConto, MutableLiveData<Conto> contoLiveData) {
        firestore.collection(collectionName)
                .document(idConto)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null) {
                        contoLiveData.setValue(null);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Conto conto = documentSnapshot.toObject(Conto.class);
                        contoLiveData.setValue(conto);
                    } else {
                        contoLiveData.setValue(null);
                    }
                });
    }

    public void updateSaldo(String idConto, double importoDaAggiungere) {
        // Prima leggi il saldo corrente
        firestore.collection(collectionName)
                .document(idConto)
                .get()  // Ottieni il documento
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Ottieni il saldo attuale dal documento
                        Double saldoAttuale = documentSnapshot.getDouble("saldo");

                        // Se il saldo attuale è null (caso di un nuovo conto), consideriamo il saldo come 0
                        if (saldoAttuale == null) {
                            saldoAttuale = 0.0;
                        }

                        // Somma il saldo attuale con l'importo passato
                        double nuovoSaldo = saldoAttuale + importoDaAggiungere;

                        // Ora aggiorna il saldo nel documento
                        firestore.collection(collectionName)
                                .document(idConto)
                                .update("saldo", nuovoSaldo)
                                .addOnSuccessListener(aVoid -> {
                                    // Saldo aggiornato correttamente
                                })
                                .addOnFailureListener(e -> {
                                    // Gestione errore
                                });
                    } else {
                        // Se il documento non esiste, possiamo gestire l'errore (ad esempio creando il conto)
                    }
                })
                .addOnFailureListener(e -> {
                    // Gestione errore lettura del documento
                });
    }

    public LiveData<Double> getSaldoById(String idConto) {
        MutableLiveData<Double> saldoLiveData = new MutableLiveData<>();

        firestore.collection(collectionName)
                .document(idConto)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null) {
                        saldoLiveData.setValue(null);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        // Ottieni il saldo direttamente dal documento
                        Double saldo = documentSnapshot.getDouble("saldo"); // Assumendo "saldo" sia il campo
                        saldoLiveData.setValue(saldo);
                    } else {
                        saldoLiveData.setValue(null); // Se il documento non esiste
                    }
                });

        return saldoLiveData;
    }
    public void getNomeContoById(String idConto, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        // Prova prima a leggere dalla cache
        firestore.collection(collectionName)
                .document(idConto)
                .get(Source.CACHE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        onCompleteListener.onComplete(task); // Nome trovato nella cache
                    } else {
                        // Se non trovato nella cache, prova a ottenere dal server
                        firestore.collection(collectionName)
                                .document(idConto)
                                .get(Source.SERVER)
                                .addOnCompleteListener(onCompleteListener);
                    }
                });
    }
}
