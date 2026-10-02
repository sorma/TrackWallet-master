package com.jm.trackwallet.ui.home;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.jm.trackwallet.R;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.Categoria;
import com.jm.trackwallet.model.Conto;
import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModel;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelCategoria;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelConto;
import com.jm.trackwallet.manager.PreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private PreferencesManager preferencesManager;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Context context;
    String idCategoria = "", idConto = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesManager preferencesManager = new PreferencesManager(this);

        if (preferencesManager.getTema()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // Tema chiaro
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);  // Tema scuro
        }

        setAppLanguage(preferencesManager.getLanguage());


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationUI.setupWithNavController(bottomNav, navController);

        FloatingActionButton aggiungiTrasazione = findViewById(R.id.aggiungi_transazione);
        aggiungiTrasazione.setOnClickListener(v -> {
            showTransactionDialog();
        });
    }

    private void showTransactionDialog() {
        // Crea un builder per il dialogo
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Inflattiamo il layout del dialogo
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_aggiungi_transazione, null);

        // Imposta il layout del dialogo
        builder.setView(dialogView);

        // Inizializza gli elementi del layout
        TextInputEditText editTextImporto = dialogView.findViewById(R.id.editText_importo);
        TextInputEditText editTextDataOra = dialogView.findViewById(R.id.editText_data_ora);
        TextInputEditText editTextTitolo = dialogView.findViewById(R.id.editText_titolo);
        RadioButton radioSpesa = dialogView.findViewById(R.id.radio_spesa);
        RadioButton radioEntrata = dialogView.findViewById(R.id.radio_entrata);
        Spinner spinnerConto = dialogView.findViewById(R.id.spinner_conto);
        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinner_categoria);
        Button btnAggiungi = dialogView.findViewById(R.id.buttonAggiungi);
        Button btnAnnulla = dialogView.findViewById(R.id.buttonAnnulla); // Se necessario aggiungere un pulsante di annullamento

        PreferencesManager pm = new PreferencesManager(context);
        if (pm.getCurrency().equals("€"))
            editTextImporto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.euro, 0);
        else if (pm.getCurrency().equals("$")) {
            editTextImporto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dollar, 0);
        } else
            editTextImporto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pound, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editTextDataOra.setText(dateFormat.format(new Date()));
        // Personalizza il dialogo per la larghezza full-screen
        AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,  // Larghezza a schermo intero
                WindowManager.LayoutParams.WRAP_CONTENT   // Altezza a schermo intero
        );
        // Gestione del clic sul campo DataOra per aprire il DatePicker
        editTextDataOra.setOnClickListener(v -> {
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
                editTextDataOra.setText(formattedDate); // Impostiamo la data nell'EditText
            });

            // Mostriamo il MaterialDatePicker
            materialDatePicker.show(getSupportFragmentManager(), materialDatePicker.toString());
        });

        // Ascoltatore per il cambio di RadioButton
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
        // Gestione del click del pulsante Aggiungi

        btnAggiungi.setOnClickListener(v -> {
            String importoStr = editTextImporto.getText().toString();
            String dataOraStr = editTextDataOra.getText().toString();
            String titoloStr = editTextTitolo.getText().toString();
            String format = "dd/MM/yyyy"; // formato della stringa


            if (!importoStr.isEmpty() && !dataOraStr.isEmpty() && !titoloStr.isEmpty() && !idCategoria.isEmpty() && !idConto.isEmpty() && (radioSpesa.isChecked() || radioEntrata.isChecked())) {
                // Convertiamo l'importo in double
                double importo = Double.parseDouble(importoStr);
                boolean isSpesa = radioSpesa.isChecked();
                Timestamp timestamp = null;
                try {
                    // Creiamo un oggetto SimpleDateFormat con il formato specificato
                    SimpleDateFormat dateF = new SimpleDateFormat(format);

                    // Convertiamo la stringa in un oggetto Date
                    Date date = dateF.parse(dataOraStr);

                    // Ora creiamo un Timestamp di Firebase dalla data
                    timestamp = new Timestamp(date);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (((isSpesa && importo < 0.0) || (!isSpesa && importo >= 0.0)) && timestamp != null) {
                    // Crea una nuova transazione
                    if (!pm.getCurrency().equals("€")) {
                        importo /= pm.getExchangeRate();
                    }
                    Transazione transazione = new Transazione(
                            "",
                            titoloStr,
                            FirebaseManager.getInstance().getCurrentUser().getUid(), // ID dell'utente
                            idConto,
                            idCategoria,
                            importo,
                            timestamp
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

                    // Chiudi il dialogo
                    dialog.dismiss();

                    // Mostra un toast di conferma
                } else {
                    Toast.makeText(this, R.string.spesa_non_negativa_entrata_non_positiva, Toast.LENGTH_SHORT).show();

                }

            } else {
                Toast.makeText(this, R.string.compila_tutti_i_campi, Toast.LENGTH_SHORT).show();
            }
        });

        // Gestione del pulsante Annulla
        btnAnnulla.setOnClickListener(v -> dialog.dismiss());

        // Mostriamo il dialogo
        dialog.show();
    }

    private void setAppLanguage(String languageCode) {
        Locale locale;

        // Mappa il codice della lingua al Locale
        switch (languageCode) {
            case "ITA":
                locale = new Locale("it");
                break;
            case "ESP":
                locale = new Locale("es");
                break;
            case "ENG":
                locale = new Locale("en");
                break;
            case "FR":
                locale = new Locale("fr");
                break;
            default:
                locale = new Locale("it");  // Imposta l'italiano come lingua di default
                break;
        }

        // Imposta la lingua locale
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        // Applica la configurazione alla risorsa
        context = createConfigurationContext(config);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void updateSpinnersForEntrata(Spinner spinnerConto, Spinner spinnerCategoria) {
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
        dbCategoriaRepository.getCategorie(FirebaseManager.getInstance().getCurrentUser().getUid(), true)
                .observe(this, categorieList -> {
                    if (categorieList != null) {
                        // Crea un adattatore per categorie
                        ArrayAdapter<Categoria> categoriaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorieList);
                        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategoria.setAdapter(categoriaAdapter);
                    }
                });

        DBContoRepository dbContoRepository = new DBContoRepository();
        dbContoRepository.getContiByUserId(FirebaseManager.getInstance().getCurrentUser().getUid())
                .observe(this, contiList -> {
                    if (contiList != null) {
                        // Crea un adattatore per conti
                        ArrayAdapter<Conto> contoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contiList);
                        contoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerConto.setAdapter(contoAdapter);
                    }
                });
        spinnerConto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Conto selectedConto = (Conto) parentView.getItemAtPosition(position); // Ottieni l'oggetto Conto
                idConto = selectedConto.getIdConto(); // Ottieni l'ID del conto
                // Fai quello che ti serve con idConto
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                idConto = "";
            }
        });

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Categoria selectedCategoria = (Categoria) parentView.getItemAtPosition(position); // Ottieni l'oggetto Categoria
                idCategoria = selectedCategoria.getIdCategoria(); // Ottieni l'ID della categoria
                // Fai quello che ti serve con idCategoria
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Gestisci quando non è selezionato nulla
                idCategoria = "";
            }
        });
    }

    private void updateSpinnersForSpesa(Spinner spinnerConto, Spinner spinnerCategoria) {
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
        dbCategoriaRepository.getCategorie(FirebaseManager.getInstance().getCurrentUser().getUid(), false)
                .observe(this, categorieList -> {
                    if (categorieList != null) {
                        // Crea un adattatore per categorie
                        ArrayAdapter<Categoria> categoriaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorieList);
                        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategoria.setAdapter(categoriaAdapter);
                    }
                });

        DBContoRepository dbContoRepository = new DBContoRepository();
        dbContoRepository.getContiByUserId(FirebaseManager.getInstance().getCurrentUser().getUid())
                .observe(this, contiList -> {
                    if (contiList != null) {
                        // Crea un adattatore per conti
                        ArrayAdapter<Conto> contoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contiList);
                        contoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerConto.setAdapter(contoAdapter);
                    }
                });
        spinnerConto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Conto selectedConto = (Conto) parentView.getItemAtPosition(position); // Ottieni l'oggetto Conto
                idConto = selectedConto.getIdConto(); // Ottieni l'ID del conto
                // Fai quello che ti serve con idConto
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                idConto = "";
            }
        });

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Categoria selectedCategoria = (Categoria) parentView.getItemAtPosition(position); // Ottieni l'oggetto Categoria
                idCategoria = selectedCategoria.getIdCategoria(); // Ottieni l'ID della categoria
                // Fai quello che ti serve con idCategoria
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Gestisci quando non è selezionato nulla
                idCategoria = "";
            }
        });
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Per Android 6.0+ (API 23+)
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // Per Android 5.1 e versioni precedenti (API <23)
            android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

}