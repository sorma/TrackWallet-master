package com.jm.trackwallet.repository.interfaces;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jm.trackwallet.model.Categoria;

import java.util.List;


public interface CategoriaRepository {
    String collectionName = "categorie";

    LiveData<List<Categoria>> getCategorie(String idUtente,boolean tipo);

    void deleteCategoria(String idCategoria);
    void addCategoria(Categoria categoria);
     Task<DocumentSnapshot> getCategoriaByIdGraph(String idCategoria);
    LiveData<Categoria> getCategoriaById(String idCategoria);

    void getNomeCategoriaById(String idCategoria, OnCompleteListener<DocumentSnapshot> onCompleteListener);
}

