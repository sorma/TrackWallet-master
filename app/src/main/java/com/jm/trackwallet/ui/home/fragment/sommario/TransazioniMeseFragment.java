package com.jm.trackwallet.ui.home.fragment.sommario;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.jm.trackwallet.R;
import com.jm.trackwallet.adapter.AdapterTransazione;
import com.jm.trackwallet.adapter.CustomSpinnerAdapter;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.components.TransazioneDiffCallback;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TransazioniMeseFragment extends Fragment {
    private TransazioneViewModel viewModel;
    private RecyclerView recyclerView;
    private AdapterTransazione adapter;
    private String mese;
    private int anno;
    private TextInputEditText searchEditText;
    private Spinner spinner;
    TextView nome;
    MaterialToolbar appBar;
    int lastSelectedPosition = -1;
    private List<Transazione> transazioni;
    private List<Transazione> transazioniFiltered;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transazioni_mese, container, false);
        recyclerView = view.findViewById(R.id.recyclerTransazioni);
        searchEditText = view.findViewById(R.id.searchEditText);
        spinner = view.findViewById(R.id.spinner);
        appBar = view.findViewById(R.id.topAppBar);
        nome = view.findViewById(R.id.nome);
        // Recupera i dati del mese e anno passati dal fragment precedente
        mese = getArguments().getString("mese");
        anno = getArguments().getInt("anno");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        transazioni = new ArrayList<>();
        transazioniFiltered = new ArrayList<>();
        adapter = new AdapterTransazione(getContext(), transazioni);
        recyclerView.setAdapter(adapter);

        int meseId = Integer.parseInt(mese);

        nome.setText(getContext().getString(meseId) + " " + anno);
        inizializza_spinner();


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterTransazioniSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        appBar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            if (navController.getPreviousBackStackEntry() != null) {
                navController.navigateUp();
            }
        });
        // Ottieni il ViewModel
        viewModel = new ViewModelProvider(this).get(TransazioneViewModel.class);
        DBTransazioneRepository dbTransazioneRepository = new DBTransazioneRepository();
        dbTransazioneRepository.getTransazioniByUtente(FirebaseManager.getInstance().getCurrentUser().getUid()).observe(getViewLifecycleOwner(), transazioniUtente -> {
            viewModel.setTransazioni(transazioniUtente);
            viewModel.filtraTransazioniPerMese(mese, anno);
        });
        // Filtra le transazioni per mese e anno


        // Osserva le transazioni filtrate
        viewModel.getTransazioniFiltrateLiveData().observe(getViewLifecycleOwner(), transazioniList -> {
            if (transazioniList != null) {
                // Usa DiffUtil per aggiornare la lista solo se sono cambiate
                updateTransazioni(transazioniList);
                filterTransazioniSearch(searchEditText.getText().toString());
            }
        });

        return view;
    }

    private void updateTransazioni(List<Transazione> transazioniList) {
        if (transazioniList.isEmpty()) {
            adapter.notifyItemRemoved(0);
            transazioni.clear();
            transazioniFiltered.clear();
        } else {
            TransazioneDiffCallback diffCallback = new TransazioneDiffCallback(transazioni, transazioniList);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            transazioni.clear();
            transazioni.addAll(transazioniList);
            transazioniFiltered.clear();
            transazioniFiltered.addAll(transazioniList);

            diffResult.dispatchUpdatesTo(adapter);
        }
    }

    private void inizializza_spinner() {
        String[] filterOptions = {getString(R.string.decr), getString(R.string.cresc), getString(R.string.decr), getString(R.string.cresc)};
        int[] icons = {R.drawable.calendario, R.drawable.calendario, R.drawable.importo, R.drawable.importo};

        // Crea un ArrayAdapter personalizzato per il Spinner
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), filterOptions, icons);
        spinner.setAdapter(adapter);

        // Aggiungi un listener per l'elemento selezionato
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                // Fai qualcosa quando selezioni un elemento
                ordina(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Gestisci quando non è selezionato niente
            }
        });
    }

    private void filterTransazioniSearch(String query) {
        List<Transazione> filteredList = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (Transazione transazione : transazioniFiltered) {
            String title = transazione.getTitolo().toLowerCase();
            Date data = new Date(transazione.getData().getSeconds() * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = dateFormat.format(data).toLowerCase();
            String amount = String.valueOf(transazione.getImporto()).toLowerCase();

            if (title.contains(queryLower) || date.contains(queryLower) || amount.contains(queryLower)) {
                filteredList.add(transazione);
            }
        }

        TransazioneDiffCallback diffCallback = new TransazioneDiffCallback(transazioni, filteredList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        transazioni.clear();
        transazioni.addAll(filteredList);
        diffResult.dispatchUpdatesTo(adapter);
    }

    private void ordina(int position) {
        if (lastSelectedPosition != position) {
            List<Transazione> oldList = new ArrayList<>(transazioni);

            if (position == 0) { // Ordinamento per data decrescente
                ordinaPerDataDecrescente();
            } else if (position == 1) { // Ordinamento per data crescente
                ordinaPerDataCrescente();
            } else if (position == 2) { // Ordinamento per importo decrescente
                ordinaPerImportoDecrescente();
            } else if (position == 3) { // Ordinamento per importo crescente
                ordinaPerImportoCrescente();
            }

            // Usa DiffUtil per aggiornare l'adapter
            TransazioneDiffCallback diffCallback = new TransazioneDiffCallback(oldList, transazioni);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            diffResult.dispatchUpdatesTo(adapter);

            lastSelectedPosition = position;
        }
    }

    // Metodo per ordinare per data decrescente
    private void ordinaPerDataDecrescente() {
        transazioni.sort((t1, t2) -> t2.getData().compareTo(t1.getData())); // Decrescente
        viewModel.setTransazioni(transazioni);
    }

    // Metodo per ordinare per data crescente
    private void ordinaPerDataCrescente() {
        transazioni.sort((t1, t2) -> t1.getData().compareTo(t2.getData())); // Crescente
        viewModel.setTransazioni(transazioni);
    }

    // Metodo per ordinare per importo decrescente
    private void ordinaPerImportoDecrescente() {
        transazioni.sort((t1, t2) -> Double.compare(t2.getImporto(), t1.getImporto())); // Decrescente
        viewModel.setTransazioni(transazioni);

    }

    // Metodo per ordinare per importo crescente
    private void ordinaPerImportoCrescente() {
        transazioni.sort((t1, t2) -> Double.compare(t1.getImporto(), t2.getImporto())); // Crescente
        viewModel.setTransazioni(transazioni);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ottieni la BottomNavigationView
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            // Prima deseleziona tutto
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            // Ora seleziona solo la voce corretta
            bottomNavigationView.getMenu().findItem(R.id.sommarioFragment).setChecked(true);
        }
    }

}
