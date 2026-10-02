package com.jm.trackwallet.ui.home.fragment.impostazioni;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.jm.trackwallet.R;
import com.jm.trackwallet.adapter.AdapterAbbonamento;
import com.jm.trackwallet.repository.DBAbbonamentoRepository;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.Abbonamento;
import com.jm.trackwallet.model.Categoria;
import com.jm.trackwallet.model.Conto;
import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.ui.components.AbbonamentoDiffCallback;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModel;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelCategoria;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelConto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link abbonamentiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class abbonamentiFragment extends Fragment {

    String idCategoria = "", idConto = "";
    private RecyclerView abbonamentiRecyclerView;
    private List<Abbonamento> abbonamenti;
    private DBAbbonamentoRepository dbAbbonamentoRepository;
    private AdapterAbbonamento adattatoreAbbonamento;


    public abbonamentiFragment() {
        // Required empty public constructor
    }

    public static abbonamentiFragment newInstance() {
        abbonamentiFragment fragment = new abbonamentiFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_abbonamenti, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar appBar = getView().findViewById(R.id.topAppBar);
        dbAbbonamentoRepository = new DBAbbonamentoRepository();
        appBar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            if (navController.getPreviousBackStackEntry() != null) {
                navController.navigateUp();
            }
        });

        Button aggiungi = view.findViewById(R.id.buttonAggiungi);
        aggiungi.setOnClickListener(v -> {
            showTransactionDialog();
        });

        super.onViewCreated(view, savedInstanceState);
        abbonamentiRecyclerView = view.findViewById(R.id.recyclerAbbonamenti);


        // Usa LinearLayoutManager con orientamento verticale
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        abbonamentiRecyclerView.setLayoutManager(layoutManager);

        // Crea l'adapter e imposta RecyclerView
        abbonamenti = new ArrayList<>();
        adattatoreAbbonamento = new AdapterAbbonamento(getContext(), abbonamenti);
        abbonamentiRecyclerView.setAdapter(adattatoreAbbonamento);

        // Osserva le categorie per aggiornare l'UI quando i dati cambiano
        dbAbbonamentoRepository.getAbbonamentiByUserId(FirebaseManager.getInstance().getCurrentUser().getUid())
                .observe(getViewLifecycleOwner(), a -> {
                    if (a != null) {
                        Log.d("Firestore", "Abbonamenti ricevuti: " + a.size());

                        List<Abbonamento> nuovaLista = new ArrayList<>(a);
                        AbbonamentoDiffCallback diffCallback = new AbbonamentoDiffCallback(abbonamenti, nuovaLista);
                        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

                        abbonamenti.clear();
                        abbonamenti.addAll(nuovaLista);
                        diffResult.dispatchUpdatesTo(adattatoreAbbonamento);
                    }
                });
    }

    private void showTransactionDialog() {
        // Crea un builder per il dialogo
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        // Inflattiamo il layout del dialogo
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_aggiungi_abbonamento, null);

        // Imposta il layout del dialogo
        builder.setView(dialogView);

        // Inizializza gli elementi del layout
        TextInputEditText editTextImporto = dialogView.findViewById(R.id.editText_importo);
        TextInputEditText editTextDataInizio = dialogView.findViewById(R.id.editText_data_inizio);
        TextInputEditText editTextDataFine = dialogView.findViewById(R.id.editText_data_fine);
        TextInputEditText editTextTitolo = dialogView.findViewById(R.id.editText_titolo);
        RadioButton radioSpesa = dialogView.findViewById(R.id.radio_spesa);
        RadioButton radioEntrata = dialogView.findViewById(R.id.radio_entrata);
        Spinner spinnerConto = dialogView.findViewById(R.id.spinner_conto);
        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinner_categoria);
        Button btnAggiungi = dialogView.findViewById(R.id.buttonAggiungi);
        Button btnAnnulla = dialogView.findViewById(R.id.buttonAnnulla); // Se necessario aggiungere un pulsante di annullamento
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editTextDataInizio.setText(dateFormat.format(new Date()));
        editTextDataFine.setText(dateFormat.format(new Date()));
        // Personalizza il dialogo per la larghezza full-screen
        AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,  // Larghezza a schermo intero
                WindowManager.LayoutParams.WRAP_CONTENT   // Altezza a schermo intero
        );
        // Gestione del clic sul campo DataOra per aprire il DatePicker
        editTextDataInizio.setOnClickListener(v -> {
            // Creiamo il MaterialDatePicker
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.seleziona_una_data)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Imposta la data odierna come predefinita
                    .build();

            // Gestiamo la selezione della data
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                // Convertiamo la data selezionata in formato "dd/MM/yyyy"
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = format.format(new Date(selection));
                editTextDataInizio.setText(formattedDate); // Impostiamo la data nell'EditText
            });

            // Mostriamo il MaterialDatePicker
            materialDatePicker.show(this.getChildFragmentManager(), materialDatePicker.toString());
        });

        PreferencesManager pm = new PreferencesManager(requireContext());
        if (pm.getCurrency().equals("€"))
            editTextImporto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.euro, 0);
        else if (pm.getCurrency().equals("$")) {
            editTextImporto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dollar, 0);
        } else
            editTextImporto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pound, 0);

        editTextDataFine.setOnClickListener(v -> {
            // Creiamo il MaterialDatePicker
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.seleziona_una_data)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Imposta la data odierna come predefinita
                    .build();

            // Gestiamo la selezione della data
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                // Convertiamo la data selezionata in formato "dd/MM/yyyy"
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = format.format(new Date(selection));
                editTextDataFine.setText(formattedDate); // Impostiamo la data nell'EditText
            });

            // Mostriamo il MaterialDatePicker
            materialDatePicker.show(this.getChildFragmentManager(), materialDatePicker.toString());
        });


        radioEntrata.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Aggiorna gli spinner per l'entrata
                editTextImporto.setText("");
                updateSpinnersForEntrata(spinnerConto, spinnerCategoria);
            }
        });

        radioSpesa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Aggiorna gli spinner per la spesa
                editTextImporto.setText("-");
                updateSpinnersForSpesa(spinnerConto, spinnerCategoria);
            }
        });

        radioSpesa.setChecked(true);

        btnAggiungi.setOnClickListener(v -> {


            String importoStr = editTextImporto.getText().toString();
            String dataInizioStr = editTextDataInizio.getText().toString();
            String dataFineStr = editTextDataFine.getText().toString();
            String nomeStr = editTextTitolo.getText().toString();
            String format = "dd/MM/yyyy"; // formato della stringa


            if (!importoStr.isEmpty() && !dataInizioStr.isEmpty() && !dataFineStr.isEmpty() && !nomeStr.isEmpty() && !idCategoria.isEmpty() && !idConto.isEmpty() && (radioSpesa.isChecked() || radioEntrata.isChecked())) {
                // Convertiamo l'importo in double
                double importo = Double.parseDouble(importoStr);
                boolean isSpesa = radioSpesa.isChecked();
                Timestamp timestampInizio = null;
                Timestamp timestampFine = null;

                SimpleDateFormat dateF = new SimpleDateFormat(format);
                Date date;
                try {
                    date = dateF.parse(dataInizioStr);
                    timestampInizio = new Timestamp(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    date = dateF.parse(dataFineStr);
                    timestampFine = new Timestamp(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (((isSpesa && importo < 0.0) || (!isSpesa && importo >= 0.0)) && timestampInizio != null && timestampFine != null) {
                    if (!pm.getCurrency().equals("€")) {
                        importo /= pm.getExchangeRate();
                    }

                    Abbonamento abbonamento = new Abbonamento(
                            "",
                            FirebaseManager.getInstance().getCurrentUser().getUid(), // ID dell'utente
                            nomeStr,
                            idConto,
                            idCategoria,
                            importo,
                            timestampInizio,
                            timestampFine
                    );
                    DBAbbonamentoRepository dbAbbonamentoRepository = new DBAbbonamentoRepository();
                    dbAbbonamentoRepository.addAbbonamento(abbonamento);
                    dialog.dismiss();
                    Toast.makeText(getContext(), R.string.abbonamento_aggiunto, Toast.LENGTH_SHORT).show();

                    Date oggi = new Date();
                    SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String oggiStr = formatoData.format(oggi);
                    if(oggiStr.equals(dataInizioStr)){
                        Transazione transazione = new Transazione(
                                "",
                                nomeStr,
                                FirebaseManager.getInstance().getCurrentUser().getUid(), // ID dell'utente
                                idConto,
                                idCategoria,
                                importo,
                                timestampInizio
                        );
                        TransazioneViewModelCategoria transazioneViewModelCategoria = new ViewModelProvider(this).get(TransazioneViewModelCategoria.class);
                        transazioneViewModelCategoria.addTransazione(transazione);

                        TransazioneViewModelConto transazioneViewModelConto = new ViewModelProvider(this).get(TransazioneViewModelConto.class);
                        transazioneViewModelConto.addTransazione(transazione);

                        TransazioneViewModel transazioneViewModel = new ViewModelProvider(this).get(TransazioneViewModel.class);
                        transazioneViewModel.addTransazione(transazione);



                        // Salviamo la transazione nel database
                        DBTransazioneRepository dbTransazioneRepository = new DBTransazioneRepository();
                        dbTransazioneRepository.addTransazione(transazione);
                        String conto = transazione.getIdConto();
                        double imp = transazione.getImporto();

                        DBContoRepository dbContoRepository = new DBContoRepository();
                        dbContoRepository.updateSaldo(conto, imp);
                    }

                    Toast.makeText(getContext(), R.string.abbonamento_aggiunto, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.errore_nei_dati, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), R.string.compila_tutti_i_campi, Toast.LENGTH_SHORT).show();
            }
        });
        btnAnnulla.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateSpinnersForEntrata(Spinner spinnerConto, Spinner spinnerCategoria) {
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
        dbCategoriaRepository.getCategorie(FirebaseManager.getInstance().getCurrentUser().getUid(), true)
                .observe(getActivity(), categorieList -> {
                    if (categorieList != null) {
                        ArrayAdapter<Categoria> categoriaAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categorieList);
                        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategoria.setAdapter(categoriaAdapter);
                    }
                });

        DBContoRepository dbContoRepository = new DBContoRepository();
        dbContoRepository.getContiByUserId(FirebaseManager.getInstance().getCurrentUser().getUid())
                .observe(getActivity(), contiList -> {
                    if (contiList != null) {
                        ArrayAdapter<Conto> contoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, contiList);
                        contoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerConto.setAdapter(contoAdapter);
                    }
                });
        spinnerConto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Conto selectedConto = (Conto) parentView.getItemAtPosition(position);
                idConto = selectedConto.getIdConto();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                idConto="";
            }
        });

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Categoria selectedCategoria = (Categoria) parentView.getItemAtPosition(position);
                idCategoria = selectedCategoria.getIdCategoria();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                idCategoria="";
            }
        });
    }

    private void updateSpinnersForSpesa(Spinner spinnerConto, Spinner spinnerCategoria) {
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
        dbCategoriaRepository.getCategorie(FirebaseManager.getInstance().getCurrentUser().getUid(), false)
                .observe(getActivity(), categorieList -> {
                    if (categorieList != null) {
                        ArrayAdapter<Categoria> categoriaAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categorieList);
                        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategoria.setAdapter(categoriaAdapter);
                    }
                });

        DBContoRepository dbContoRepository = new DBContoRepository();
        dbContoRepository.getContiByUserId(FirebaseManager.getInstance().getCurrentUser().getUid())
                .observe(getActivity(), contiList -> {
                    if (contiList != null) {
                        ArrayAdapter<Conto> contoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, contiList);
                        contoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerConto.setAdapter(contoAdapter);
                    }
                });
        spinnerConto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Conto selectedConto = (Conto) parentView.getItemAtPosition(position);
                idConto = selectedConto.getIdConto();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                idConto="";
            }
        });

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Categoria selectedCategoria = (Categoria) parentView.getItemAtPosition(position);
                idCategoria = selectedCategoria.getIdCategoria();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                idCategoria="";
            }
        });
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
            bottomNavigationView.getMenu().findItem(R.id.impostazioniFragment).setChecked(true);
        }
    }
}


/*Di seguito il codice della cloudFunction caricata su firebase che permette di verificare se la data
* odierna è la data di rinnovo dell'abbonamento e in quel caso provvede a creare una nuova transazione
* inserendo i dati correttamente*/

/*const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { Timestamp } = require("firebase-admin/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");


admin.initializeApp();

// Cloud Function che controlla i rinnovi ogni 24 ore
exports.controllaRinnovi = onSchedule({
    schedule: '0 0 * * *',
    timeZone: 'Europe/Rome'
}, async (event) => {
    const oggi = new Date(); // Data corrente
    const db = admin.firestore();

    try {
        const abbonamentiSnapshot = await db.collection("abbonamenti").get();

        for (const doc of abbonamentiSnapshot.docs) {
            const abbonamento = doc.data();
            const dataInizio = abbonamento.dataInizio.toDate(); // Converte il Timestamp in Date
            dataInizio.setDate(dataInizio.getDate() + 1); //questo perchè a causa del fusorario
                                                          //torna indietro di un giorno quando si passa
                                                          //da timestamp a date
            const dataFine = abbonamento.dataFine.toDate(); // Converte il Timestamp in Date
            dataFine.setDate(dataFine.getDate() + 1); //questo perchè a causa del fusorario
                                                          //torna indietro di un giorno quando si passa
                                                          //da timestamp a date
            var dataRinnovo = new Date(dataInizio);

            while (dataRinnovo.getFullYear() <= oggi.getFullYear() &&
            dataRinnovo.getMonth() <= oggi.getMonth() &&
            dataRinnovo.getFullYear() <= dataFine.getFullYear() &&
            dataRinnovo.getMonth() <= dataFine.getMonth()) {

                if (dataRinnovo.getFullYear() == oggi.getFullYear() &&
                dataRinnovo.getMonth() == oggi.getMonth() &&
                dataRinnovo.getDate() == oggi.getDate()) {
                    console.log(`Creazione transazione per l'abbonamento ${abbonamento.idAbbonamento}`);
                    await creaTransazione(abbonamento);
                    break; // Uscire dal ciclo una volta trovato il rinnovo
                }

                let giornoOriginale = dataRinnovo.getDate();
                let nextMonth = dataRinnovo.getMonth() + 1;
                let nextYear = dataRinnovo.getFullYear();

                // Se il mese è dicembre (11), passa a gennaio dell'anno successivo
                if (nextMonth > 11) {
                    nextMonth = 0;
                    nextYear += 1;
                }

                // Prova a impostare lo stesso giorno del mese successivo
                let nextRenewal = new Date(nextYear, nextMonth, giornoOriginale);

                // Se il giorno cambia, significa che il mese non ha quel giorno -> usa l'ultimo giorno del mese
                if (nextRenewal.getDate() !== giornoOriginale) {
                    nextRenewal = new Date(nextYear, nextMonth + 1, 0);
                }

                dataRinnovo = nextRenewal;

                //console.log(`Nuovo rinnovo string ` + dataRinnovo.toLocaleDateString("it-IT"));
            }
        }
    } catch (error) {
        console.error("Errore durante il controllo dei rinnovi:", error);
    }
});

// Funzione per creare una nuova transazione
async function creaTransazione(abbonamento) {
    const db = admin.firestore();
    const id = getNewId("transazioni");

    const nuovaTransazione = {
        idUtente: abbonamento.idUtente,
        importo: abbonamento.importo,
        titolo: abbonamento.nome,
        idTransazione: id,
        idConto: abbonamento.conto,
        idCategoria: abbonamento.categoria,
        data: Timestamp.now(), // Usa Timestamp.now() per la data attuale
    };

    try {
        await db.collection("transazioni").doc(id).set(nuovaTransazione);
        const contoRef = db.collection("conti").doc(abbonamento.conto);
        const contoDoc = await contoRef.get();
        const saldoAttuale = contoDoc.data().saldo;
        const nuovoSaldo = saldoAttuale + abbonamento.importo;
        await contoRef.update({ saldo: nuovoSaldo });
    } catch (error) {
        console.error("Errore nella creazione della transazione:", error);
    }
}

// Funzione per generare un nuovo ID documento in Firestore
function getNewId(collectionName) {
    return admin.firestore().collection(collectionName).doc().id;
}*/