package com.jm.trackwallet.ui.home.fragment.categorie;

import static com.jm.trackwallet.util.Constants.iconListConto;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.jm.trackwallet.R;
import com.jm.trackwallet.adapter.AdapterTransazione;
import com.jm.trackwallet.adapter.CustomSpinnerAdapter;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.Conto;
import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.components.TransazioneDiffCallback;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelConto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransazioniContoFragment extends Fragment {

    String contoId;
    Conto conto;
    private DBTransazioneRepository dbTransazioneRepository;
    private DBContoRepository dbContoRepository;
    private List<Transazione> transazioni;
    private List<Transazione> transazioniFiltered;
    private RecyclerView recyclerView;
    private AdapterTransazione adapter;
    private TextInputEditText searchEditText;
    private Spinner spinner;
    TextView nome;
    ImageView contoIcona;
    MaterialToolbar appBar;
    int lastSelectedPosition=-1;

    private TransazioneViewModelConto transactionViewModel;

    public TransazioniContoFragment() {
        // Required empty public constructor
    }


    public static TransazioniContoFragment newInstance(String param1, String param2) {
        TransazioniContoFragment fragment = new TransazioniContoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbTransazioneRepository = new DBTransazioneRepository();
        dbContoRepository = new DBContoRepository();

        // Ottieni il ViewModel
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransazioneViewModelConto.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transazioni_conto, container, false);
        recyclerView = view.findViewById(R.id.recyclerTransazioni);
        searchEditText = view.findViewById(R.id.searchEditText);
        spinner = view.findViewById(R.id.spinner);
        appBar = view.findViewById(R.id.topAppBar);
        nome = view.findViewById(R.id.nome);

        transazioniFiltered = new ArrayList<>();
        if (getArguments() != null) {
            contoId = getArguments().getString("contoId");
            transactionViewModel.setContoId(contoId);
        }

        loadContoDetails(contoId);
        inizializza_spinner();


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterTransazioniSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        // Osserva le transazioni dal ViewModel per aggiornamenti
        transactionViewModel.getTransazioniLiveData().observe(getViewLifecycleOwner(), transazioniList -> {
            if (transazioniList != null) {
                // Usa DiffUtil per aggiornare la lista solo se sono cambiate
                updateTransazioni(transazioniList);
                filterTransazioniSearch(searchEditText.getText().toString());
            }
        });

        return view;
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
    private void loadContoDetails(String contoId) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        transazioni = new ArrayList<>();
        adapter = new AdapterTransazione(getContext(), transazioni);
        recyclerView.setAdapter(adapter);

        LiveData<Conto> cont = dbContoRepository.getContoById(contoId);

        cont.observe(getViewLifecycleOwner(), c -> {
            if (c != null) {
                conto = c;
                updateUI(conto);
                loadTransazioni();
            } else {
                Toast.makeText(getContext(), R.string.errore_nel_recupero_dei_dati, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Conto conto) {
        if (conto != null) {
            contoIcona = getView().findViewById(R.id.contoIcona);
            MaterialToolbar appBar = getView().findViewById(R.id.topAppBar);
            contoIcona.setImageResource(iconListConto.get(conto.getIcona()));
            nome.setText(conto.getNome());

            appBar.setNavigationOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                if (navController.getPreviousBackStackEntry() != null) {
                    navController.navigateUp();
                }
            });
        }
    }

    private void loadTransazioni() {
        dbTransazioneRepository.getTransazioniByConto(FirebaseManager.getInstance().getCurrentUser().getUid(), contoId)
                .observe(getViewLifecycleOwner(), transazioniList -> {
                    if (transazioniList != null) {
                        transactionViewModel.setTransazioni(new ArrayList<>(transazioniList)); // Setta le transazioni nel ViewModel
                    }
                });
    }

    // Funzione per aggiornare la lista delle transazioni nel Fragment
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
        transactionViewModel.setTransazioni(transazioni);
    }

    // Metodo per ordinare per data crescente
    private void ordinaPerDataCrescente() {
        transazioni.sort((t1, t2) -> t1.getData().compareTo(t2.getData())); // Crescente
        transactionViewModel.setTransazioni(transazioni);
    }

    // Metodo per ordinare per importo decrescente
    private void ordinaPerImportoDecrescente() {
        transazioni.sort((t1, t2) -> Double.compare(t2.getImporto(), t1.getImporto())); // Decrescente
        transactionViewModel.setTransazioni(transazioni);

    }

    // Metodo per ordinare per importo crescente
    private void ordinaPerImportoCrescente() {
        transazioni.sort((t1, t2) -> Double.compare(t1.getImporto(), t2.getImporto())); // Crescente
        transactionViewModel.setTransazioni(transazioni);
    }

    // Metodo per aggiornare la lista delle transazioni nel ViewModel e nell'adapter
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
            bottomNavigationView.getMenu().findItem(R.id.categorieFragment).setChecked(true);
        }
    }

}