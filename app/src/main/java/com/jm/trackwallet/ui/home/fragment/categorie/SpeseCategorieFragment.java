package com.jm.trackwallet.ui.home.fragment.categorie;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jm.trackwallet.R;
import com.jm.trackwallet.adapter.AdapterCategoria;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.model.Categoria;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.components.CategoriaDiffCallback;

import java.util.ArrayList;
import java.util.List;


public class SpeseCategorieFragment extends Fragment {

    private RecyclerView categorieRecyclerView;
    private AdapterCategoria adattatoreCategoria;
    private List<Categoria> categorie;

    private DBCategoriaRepository dbCategoriaRepository;

    public SpeseCategorieFragment() {
        // Required empty public constructor
    }

    public static SpeseCategorieFragment newInstance(String param1, String param2) {
        SpeseCategorieFragment fragment = new SpeseCategorieFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbCategoriaRepository=new DBCategoriaRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spese_categorie, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categorieRecyclerView = view.findViewById(R.id.categorieRecyclerView);

        // Imposta il layout manager per RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3); // 3 colonne per riga
        categorieRecyclerView.setLayoutManager(layoutManager);

        // Crea l'adapter e imposta RecyclerView
        categorie = new ArrayList<>();
        adattatoreCategoria = new AdapterCategoria(getContext(), categorie,false);
        categorieRecyclerView.setAdapter(adattatoreCategoria);

        // Osserva le categorie per aggiornare l'UI quando i dati cambiano
        dbCategoriaRepository.getCategorie(FirebaseManager.getInstance().getCurrentUser().getUid(), false)
                .observe(getViewLifecycleOwner(), categorieList -> {
                    if (categorieList != null) {
                        List<Categoria> nuovaLista = new ArrayList<>(categorieList);
                        nuovaLista.add(new Categoria("", "aggiungi", R.drawable.aggiungi, Color.parseColor("#c4c6d0"), false, ""));

                        CategoriaDiffCallback diffCallback = new CategoriaDiffCallback(categorie, nuovaLista);
                        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

                        categorie.clear();
                        categorie.addAll(nuovaLista);
                        diffResult.dispatchUpdatesTo(adattatoreCategoria);
                    }
                });
    }
}