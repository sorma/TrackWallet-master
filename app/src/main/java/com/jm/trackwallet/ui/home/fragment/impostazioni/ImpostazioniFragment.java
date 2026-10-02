package com.jm.trackwallet.ui.home.fragment.impostazioni;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.switchmaterial.SwitchMaterial;
import com.jm.trackwallet.R;
import com.jm.trackwallet.service.CurrencyApi;
import com.jm.trackwallet.model.ExchangeRateResponse;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.adapter.CustomSpinnerAdapter;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.home.MainActivity;
import com.jm.trackwallet.ui.welcome.WelcomeActivity;
import com.jm.trackwallet.ui.welcome.viewmodel.UtenteViewModel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ImpostazioniFragment extends Fragment {
    private UtenteViewModel utenteViewModel;
    private TextView nomeUtente;
    private PreferencesManager preferencesManager;
    private Spinner spinnerLingua;
    private Spinner spinnerValuta;
    private SwitchMaterial switchTema;
    private ImageButton logoutButton;


    public static ImpostazioniFragment newInstance() {

        return new ImpostazioniFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utenteViewModel= new ViewModelProvider(this).get(UtenteViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_impostazioni, container, false);
        preferencesManager = new PreferencesManager(requireContext());
        nomeUtente = view.findViewById(R.id.nomeUtente);

        // Carica l'utente tramite ID
        utenteViewModel.loadUtente(FirebaseManager.getInstance().getCurrentUser().getUid());

        // Ascolta gli aggiornamenti in tempo reale
        utenteViewModel.listenForRealTimeUpdates(FirebaseManager.getInstance().getCurrentUser().getUid());

        // Osserva i dati dell'utente
        utenteViewModel.getUtente().observe(getViewLifecycleOwner(), utente -> {
            if (utente != null) {
                nomeUtente.setText(utente.getUsername());
            }
        });


        Button myButton = view.findViewById(R.id.ButtonAbbonamenti);
        spinnerLingua = view.findViewById(R.id.spinnerLingua);
        spinnerValuta = view.findViewById(R.id.spinnerValuta);
        inizializza_spinnerLingua();
        inizializza_spinnerValuta();

        Button sendMessageButton = view.findViewById(R.id.ButtonSendMessage);
        EditText messageEditText = view.findViewById(R.id.etMessage);

        sendMessageButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();

            if (!message.isEmpty()) {
                sendEmail(message);
            } else {
                Toast.makeText(getContext(), R.string.per_favore_inserisci_un_messaggio, Toast.LENGTH_SHORT).show();
            }
        });


        switchTema = view.findViewById(R.id.switchTema);
        switchTema.setChecked(!preferencesManager.getTema());

        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logout());

        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setTema(!isChecked);
            requireActivity().recreate();
        });


        myButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_impostazioniFragment_to_abbonamentiFragment);
        });

        return view;
    }

    private void sendEmail(String messageContent) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"trackwalletapp@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.messaggio_da_app));
        emailIntent.putExtra(Intent.EXTRA_TEXT, messageContent);

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.invia_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.non_ci_sono_applicazioni_email_installate, Toast.LENGTH_SHORT).show();
        }
    }

    //inutilizzato
    private void applyTheme() {
        if (preferencesManager.getTema()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private void logout() {
        Toast.makeText(requireContext(), R.string.logout_effettuato, Toast.LENGTH_SHORT).show();
        FirebaseManager.getInstance().logout();
        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }


    private void inizializza_spinnerLingua() {
        String[] filterOptions = {"ITA", "ESP", "ENG", "FR"};
        int[] icons = {R.drawable.italia, R.drawable.spagna, R.drawable.inghilterra, R.drawable.francia};

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), filterOptions, icons);
        spinnerLingua.setAdapter(adapter);

        // Imposta la lingua selezionata salvata
        String savedLanguage = preferencesManager.getLanguage();
        for (int i = 0; i < filterOptions.length; i++) {
            if (filterOptions[i].equals(savedLanguage)) {
                spinnerLingua.setSelection(i); // Imposta lo spinner sulla lingua salvata
                break;
            }
        }

        // Variabile per tracciare la lingua corrente
        final String[] currentLanguage = {savedLanguage};

        // Gestisce il cambio di lingua
        spinnerLingua.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = filterOptions[position];

                // Controlla se la lingua selezionata è diversa da quella corrente
                if (!selectedLanguage.equals(currentLanguage[0])) {
                    // Aggiorna la lingua corrente
                    currentLanguage[0] = selectedLanguage;

                    // Mostra il messaggio di selezione
                    Toast.makeText(requireContext(), getString(R.string.lingua_selezionata) + selectedLanguage, Toast.LENGTH_SHORT).show();

                    // Salva la nuova lingua e applicala
                    preferencesManager.setLanguage(selectedLanguage);
                    setAppLanguage(selectedLanguage);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setAppLanguage(String languageCode) {
        // Usa un `switch` per mappare il codice della lingua a un valore Locale
        Locale locale;
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
                locale = new Locale("it"); // Default italiano
                break;
        }

        // Imposta la lingua
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        // Usa createConfigurationContext per applicare la nuova configurazione
        Context context = requireActivity().createConfigurationContext(config);
        requireActivity().getResources().updateConfiguration(config, requireActivity().getResources().getDisplayMetrics());

        Intent intent = new Intent(requireContext(), MainActivity.class);
        requireActivity().finish();
        startActivity(intent);
    }

    private void inizializza_spinnerValuta() {
        String[] filterOptions = {"EUR", "USD", "GBP"};
        int[] icons = {R.drawable.euro, R.drawable.dollar, R.drawable.pound};

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), filterOptions, icons);
        spinnerValuta.setAdapter(adapter);

        // Imposta la valuta selezionata salvata
        String savedCurrency = preferencesManager.getCurrency();
        for (int i = 0; i < filterOptions.length; i++)
            if (getSymbolFromCode(filterOptions[i]).equals(savedCurrency))
                spinnerValuta.setSelection(i);

        // Gestisce il cambio di valuta
        spinnerValuta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = filterOptions[position];
                fetchExchangeRate(selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }

    private void fetchExchangeRate(String currencyCode) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://v6.exchangerate-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

         String apiKey = getString(R.string.CURRENCY_API_KEY);
        CurrencyApi api = retrofit.create(CurrencyApi.class);
        Call<ExchangeRateResponse> call = api.getExchangeRates(apiKey);

        call.enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double rate = response.body().conversion_rates.get(currencyCode);
                    // Salva il simbolo invece del codice
                    String symbol = getSymbolFromCode(currencyCode);
                    preferencesManager.setCurrency(symbol, rate);
                    Log.d("ExchangeRate", "Tasso di cambio: 1 EUR = " + rate + " " + currencyCode);
                    updatePrices();
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                Log.e("ExchangeRate", "Errore nel caricamento: " + t.getMessage());
            }
        });

    }

    private String mapSymbolToCode(String symbol) {
        Map<String, String> currencyMap = new HashMap<>();
        currencyMap.put("€", "EUR");
        currencyMap.put("$", "USD");
        currencyMap.put("£", "GBP");

        return currencyMap.getOrDefault(symbol, "EUR"); // Default: EUR se simbolo sconosciuto
    }

    private String getSymbolFromCode(String currencyCode) {
        Map<String, String> currencyMap = new HashMap<>();
        currencyMap.put("EUR", "€");
        currencyMap.put("USD", "$");
        currencyMap.put("GBP", "£");

        return currencyMap.getOrDefault(currencyCode, "€"); // Default: €
    }

    private void updatePrices() {
        // Questo metodo aggiorna tutti i prezzi nel layout
        double exchangeRate = preferencesManager.getExchangeRate();
        // Esempio: aggiorniamo un TextView con il nuovo prezzo
        // textViewPrice.setText(String.format("%.2f", originalPrice * exchangeRate));
    }
}