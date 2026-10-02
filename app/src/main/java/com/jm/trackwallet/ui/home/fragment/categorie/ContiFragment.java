package com.jm.trackwallet.ui.home.fragment.categorie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jm.trackwallet.R;
import com.jm.trackwallet.adapter.AdapterConto;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.model.Conto;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.components.ContoDiffCallback;

import java.util.ArrayList;
import java.util.List;

public class ContiFragment extends Fragment {
    private RecyclerView contiRecyclerView;
    private AdapterConto adattatoreConto;
    private List<Conto> conti;
    private DBContoRepository dbContoRepository;


    public ContiFragment() {
        // Required empty public constructor
    }


    public static ContiFragment newInstance(String param1, String param2) {
        ContiFragment fragment = new ContiFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbContoRepository=new DBContoRepository();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conti, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contiRecyclerView = view.findViewById(R.id.recyclerViewConti);


        // Usa LinearLayoutManager con orientamento verticale
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        contiRecyclerView.setLayoutManager(layoutManager);

        // Crea l'adapter e imposta RecyclerView
        conti = new ArrayList<>();
        adattatoreConto = new AdapterConto(getContext(), conti);
        contiRecyclerView.setAdapter(adattatoreConto);

        // Osserva le categorie per aggiornare l'UI quando i dati cambiano
        dbContoRepository.getContiByUserId(FirebaseManager.getInstance().getCurrentUser().getUid())
                .observe(getViewLifecycleOwner(), contiList -> {
                    if (contiList != null) {
                        List<Conto> nuovaLista = new ArrayList<>(contiList);
                        nuovaLista.add(new Conto("", "aggiungi", R.drawable.aggiungi, "", 0.0));

                        ContoDiffCallback diffCallback = new ContoDiffCallback(conti, nuovaLista);
                        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

                        conti.clear();
                        conti.addAll(nuovaLista);
                        diffResult.dispatchUpdatesTo(adattatoreConto);
                    }
                });
    }
}