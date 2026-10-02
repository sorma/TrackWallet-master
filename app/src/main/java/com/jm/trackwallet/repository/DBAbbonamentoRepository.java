package com.jm.trackwallet.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.jm.trackwallet.model.Abbonamento;
import com.jm.trackwallet.repository.interfaces.AbbonamentoRepository;
import com.jm.trackwallet.manager.FirebaseManager;

import java.util.List;

public class DBAbbonamentoRepository implements AbbonamentoRepository {
    private final FirebaseFirestore firestore;

    public DBAbbonamentoRepository() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }

    @NonNull
    public String getNewId() {
        return firestore.collection(collectionName).document().getId();
    }


    @Override
    public LiveData<List<Abbonamento>> getAbbonamentiByUserId(String idUtente) {
        MutableLiveData<List<Abbonamento>> abbonamentiLiveData = new MutableLiveData<>();

        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .orderBy("nome", Query.Direction.ASCENDING)
                .get(Source.CACHE)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        firestore.collection(collectionName)
                                .whereEqualTo("idUtente", idUtente)
                                .orderBy("nome", Query.Direction.ASCENDING)
                                .get(Source.SERVER)
                                .addOnSuccessListener(snapshot -> {
                                    List<Abbonamento> abbonamenti = snapshot.toObjects(Abbonamento.class);
                                    abbonamentiLiveData.setValue(abbonamenti);
                                })
                                .addOnFailureListener(e -> abbonamentiLiveData.setValue(null));
                    } else {
                        List<Abbonamento> abbonamenti = querySnapshot.toObjects(Abbonamento.class);
                        abbonamentiLiveData.setValue(abbonamenti);
                    }
                })
                .addOnFailureListener(e -> {
                    firestore.collection(collectionName)
                            .whereEqualTo("idUtente", idUtente)
                            .orderBy("nome", Query.Direction.ASCENDING)
                            .get(Source.SERVER)
                            .addOnSuccessListener(snapshot -> {
                                List<Abbonamento> abbonamenti = snapshot.toObjects(Abbonamento.class);
                                abbonamentiLiveData.setValue(abbonamenti);
                            })
                            .addOnFailureListener(ex -> abbonamentiLiveData.setValue(null));
                });

        listenToRealTimeUpdates(idUtente, abbonamentiLiveData);
        return abbonamentiLiveData;
    }

    @Override
    public void addAbbonamento(Abbonamento abbonamento) {
        String idAbbonamento = getNewId();
        abbonamento.setIdAbbonamento(idAbbonamento);
        firestore.collection(collectionName)
                .document(idAbbonamento)
                .set(abbonamento)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});
    }

    @Override
    public void deleteAbbonamento(String idAbbonamento) {
        firestore.collection(collectionName)
                .document(idAbbonamento)
                .delete()
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});
    }

    private void listenToRealTimeUpdates(String idUtente, MutableLiveData<List<Abbonamento>> abbonamentiLiveData) {
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .orderBy("nome", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) {
                        abbonamentiLiveData.setValue(null);
                        return;
                    }
                    List<Abbonamento> abbonamenti = querySnapshot.toObjects(Abbonamento.class);
                    abbonamentiLiveData.setValue(abbonamenti);
                });
    }
}
