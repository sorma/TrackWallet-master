package com.jm.trackwallet.ui.home.fragment.sommario;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jm.trackwallet.R;
import com.jm.trackwallet.adapter.AdapterSommario;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.SommarioMese;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SommarioFragment extends Fragment {
    private TransazioneViewModel viewModel;
    private AdapterSommario adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sommario, container, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(this).get(TransazioneViewModel.class);
        // Adapter con il listener per il click
        adapter = new AdapterSommario(new ArrayList<>(), sommario -> {
            // Quando un utente clicca su un mese, filtra le transazioni
            viewModel.filtraTransazioniPerMese(sommario.getNomeMese(), sommario.getAnno());

            // Apri un nuovo fragment per visualizzare le transazioni di quel mese
            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle = new Bundle();
            bundle.putString("mese", sommario.getNomeMese());
            bundle.putInt("anno", sommario.getAnno());
            navController.navigate(R.id.action_sommarioFragment_to_transazioniMeseFragment, bundle);
        }, getContext());

        recyclerView.setAdapter(adapter);

        DBTransazioneRepository dbTransazioneRepository = new DBTransazioneRepository();
        dbTransazioneRepository.getTransazioniByUtente(FirebaseManager.getInstance().getCurrentUser().getUid()).observe(getViewLifecycleOwner(), transazioni -> {
            viewModel.setTransazioni(transazioni);
        });


        // Osserva il LiveData e aggiorna il RecyclerView quando cambia
        viewModel.getSommarioLiveData().observe(getViewLifecycleOwner(), sommari -> {
            // Ordina la lista sommari prima di passarlo all'adapter
            // Se gli anni sono diversi, ordina per anno
            // Se gli anni sono uguali, ordina per nomeMese
            Collections.sort(sommari,
                    Comparator.comparingInt(SommarioMese::getAnno).reversed());
            // Ora aggiorna l'adapter con la lista ordinata
            adapter.updateData(sommari);
        });

        return root;
    }


}

