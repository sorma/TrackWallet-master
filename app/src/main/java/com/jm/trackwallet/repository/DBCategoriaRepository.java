package com.jm.trackwallet.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.jm.trackwallet.model.Categoria;
import com.jm.trackwallet.repository.interfaces.CategoriaRepository;
import com.jm.trackwallet.manager.FirebaseManager;

import java.util.List;

public class DBCategoriaRepository implements CategoriaRepository {
    private final FirebaseFirestore firestore;

    @NonNull
    public String getNewId() {
        return firestore.collection(collectionName).document().getId();
    }

    public DBCategoriaRepository() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }

    @Override
    public LiveData<List<Categoria>> getCategorie(String idUtente, boolean tipo) {
        MutableLiveData<List<Categoria>> categorieLiveData = new MutableLiveData<>();

        // Prova prima a leggere dalla cache
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("tipo", tipo)
                .orderBy("nome", Query.Direction.ASCENDING)
                .get(Source.CACHE)  // Usa la cache per prima
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Se la cache è vuota, leggi dal server
                        firestore.collection(collectionName)
                                .whereEqualTo("idUtente", idUtente)
                                .whereEqualTo("tipo", tipo)
                                .orderBy("nome", Query.Direction.ASCENDING)
                                .get(Source.SERVER)  // Forza il server
                                .addOnSuccessListener(snapshot -> {
                                    List<Categoria> categorie = snapshot.toObjects(Categoria.class);
                                    categorieLiveData.setValue(categorie);
                                })
                                .addOnFailureListener(e -> {
                                    // Gestire errore lettura server
                                    categorieLiveData.setValue(null);
                                });
                    } else {
                        // Se la cache non è vuota, usa i dati letti
                        List<Categoria> categorie = querySnapshot.toObjects(Categoria.class);
                        categorieLiveData.setValue(categorie);
                    }
                })
                .addOnFailureListener(e -> {
                    // Se errore nella lettura della cache, cerca nel server
                    firestore.collection(collectionName)
                            .whereEqualTo("idUtente", idUtente)
                            .whereEqualTo("tipo", tipo)
                            .orderBy("nome", Query.Direction.ASCENDING)
                            .get(Source.SERVER)  // Forza lettura dal server
                            .addOnSuccessListener(snapshot -> {
                                List<Categoria> categorie = snapshot.toObjects(Categoria.class);
                                categorieLiveData.setValue(categorie);
                            })
                            .addOnFailureListener(ex -> {
                                categorieLiveData.setValue(null);
                            });
                });

        // Ascolta sempre per aggiornamenti in tempo reale
        listenToRealTimeUpdates(idUtente, tipo, categorieLiveData);

        return categorieLiveData;
    }

    @Override
    public void addCategoria(Categoria categoria) {
        firestore.collection(collectionName)
                .document(categoria.getIdCategoria())
                .set(categoria)
                .addOnSuccessListener(aVoid -> {
                    // Successo nell'aggiungere la categoria
                })
                .addOnFailureListener(e -> {
                    // Gestione errore
                });
    }

    public void deleteCategoria(String idCategoria) {
        firestore.collection(collectionName)
                .document(idCategoria)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // La categoria è stata eliminata con successo
                })
                .addOnFailureListener(e -> {
                    // Gestire l'errore se la cancellazione fallisce
                });
    }

    private void listenToRealTimeUpdates(String idUtente, boolean tipo, MutableLiveData<List<Categoria>> categorieLiveData) {
        firestore.collection(collectionName)
                .whereEqualTo("idUtente", idUtente)
                .whereEqualTo("tipo", tipo)
                .orderBy("nome", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) {
                        categorieLiveData.setValue(null);
                        return;
                    }

                    List<Categoria> categorie = querySnapshot.toObjects(Categoria.class);
                    categorieLiveData.setValue(categorie);
                });
    }

    public LiveData<Categoria> getCategoriaById(String idCategoria) {
        MutableLiveData<Categoria> categoriaLiveData = new MutableLiveData<>();

        // Prova prima a leggere dalla cache
        firestore.collection(collectionName)
                .document(idCategoria)
                .get(Source.CACHE)  // Usa la cache per prima
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Categoria categoria = documentSnapshot.toObject(Categoria.class);
                        categoriaLiveData.setValue(categoria);
                    } else {
                        // Se la categoria non esiste in cache, leggi dal server
                        firestore.collection(collectionName)
                                .document(idCategoria)
                                .get(Source.SERVER)  // Forza il server
                                .addOnSuccessListener(snapshot -> {
                                    if (snapshot.exists()) {
                                        Categoria categoria = snapshot.toObject(Categoria.class);
                                        categoriaLiveData.setValue(categoria);
                                    } else {
                                        categoriaLiveData.setValue(null);  // Categoria non trovata
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Gestione errore
                                    categoriaLiveData.setValue(null);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Gestione errore lettura dalla cache, quindi cerca nel server
                    firestore.collection(collectionName)
                            .document(idCategoria)
                            .get(Source.SERVER)  // Forza il server
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    Categoria categoria = snapshot.toObject(Categoria.class);
                                    categoriaLiveData.setValue(categoria);
                                } else {
                                    categoriaLiveData.setValue(null);  // Categoria non trovata
                                }
                            })
                            .addOnFailureListener(ex -> {
                                categoriaLiveData.setValue(null);
                            });
                });

        // Aggiungi ascolto in tempo reale
        listenToRealTimeUpdatesForCategoria(idCategoria, categoriaLiveData);

        return categoriaLiveData;
    }

    @Override
    public void getNomeCategoriaById(String idCategoria, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        firestore.collection(collectionName)
                .document(idCategoria)
                .get(Source.CACHE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        onCompleteListener.onComplete(task); // Nome trovato nella cache
                    } else {
                        // Se non trovato nella cache, prova a ottenere dal server
                        firestore.collection(collectionName)
                                .document(idCategoria)
                                .get(Source.SERVER)
                                .addOnCompleteListener(onCompleteListener);
                    }
                });
    }
    public Task<DocumentSnapshot> getCategoriaByIdGraph(String idCategoria) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("categorie").document(idCategoria).get();
    }


    private void listenToRealTimeUpdatesForCategoria(String idCategoria, MutableLiveData<Categoria> categoriaLiveData) {
        firestore.collection(collectionName)
                .document(idCategoria)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null) {
                        categoriaLiveData.setValue(null);
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Categoria categoria = documentSnapshot.toObject(Categoria.class);
                        categoriaLiveData.setValue(categoria);
                    } else {
                        categoriaLiveData.setValue(null);
                    }
                });
    }
}


