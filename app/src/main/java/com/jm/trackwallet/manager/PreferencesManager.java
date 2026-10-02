package com.jm.trackwallet.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "AppPreferences";
    private static final String KEY_THEME = "temaChiaro";
    private static final String KEY_LANGUAGE = "selected_language";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "currency_prefs";
    private static final String KEY_CURRENCY = "selected_currency";
    private static final String KEY_EXCHANGE_RATE = "exchange_rate";

    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setTema(boolean temaChiaro) {
        editor.putBoolean(KEY_THEME, temaChiaro);
        editor.apply();
    }

    public boolean getTema() {
        return sharedPreferences.getBoolean(KEY_THEME, true); // Default: tema chiaro
    }

    public void setCurrency(String currencySymbol, double exchangeRate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CURRENCY, currencySymbol); // Salviamo il simbolo
        editor.putFloat(KEY_EXCHANGE_RATE, (float) exchangeRate);
        editor.apply();
    }

    // Ottieni la valuta selezionata
    public String getCurrency() {
        return prefs.getString(KEY_CURRENCY, "€"); // Default: simbolo EUR ("€")
    }

    // Ottieni il tasso di cambio
    public double getExchangeRate() {
        return prefs.getFloat(KEY_EXCHANGE_RATE, 1.0f); // Default 1.0 (se è EUR)
    }

    // Salva la lingua selezionata
    public void setLanguage(String languageCode) {
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
    }

    // Ottieni la lingua selezionata
    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "ITA"); // Default: "ITA"
    }
}
