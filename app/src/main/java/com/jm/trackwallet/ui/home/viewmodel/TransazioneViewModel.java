package com.jm.trackwallet.ui.home.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jm.trackwallet.R;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.model.SommarioCategoria;
import com.jm.trackwallet.model.SommarioMese;
import com.jm.trackwallet.model.Transazione;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransazioneViewModel extends ViewModel {
    private final MutableLiveData<List<Transazione>> transazioniLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<SommarioMese>> sommarioLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Transazione>> transazioniFiltrateLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<SommarioCategoria>> sommaCategorieLiveData = new MutableLiveData<>(new ArrayList<>());

    private List<Transazione> transazioni = new ArrayList<>();

    public LiveData<List<SommarioCategoria>> getSommaCategorieLiveData() {
        return sommaCategorieLiveData;
    }

    //0 = tutte, 1=uscite, 2=entrate
    public void filtraCategorieConImportoTotale(Timestamp dataInizio, Timestamp dataFine, int tipo) {
        Date d= dataInizio.toDate();
        Date d1= dataFine.toDate();
        List<SommarioCategoria> sommaCategorie = new ArrayList<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();

        for (Transazione transazione : transazioni) {
            Timestamp data;
            data = transazione.getData();

            if (data.compareTo(dataInizio) >= 0 && data.compareTo(dataFine) <= 0) {
                String categoria = transazione.getIdCategoria();
                double importo = transazione.getImporto();

                // Controlla se la categoria esiste già nella lista
                boolean categoriaEsistente = false;

                // Scorre la lista di SommarioCategoria per vedere se la categoria è già presente
                for (SommarioCategoria sommario : sommaCategorie) {
                    if (sommario.getIdCategoria().equals(categoria)) {
                        // Se la categoria esiste, somma l'importo
                        sommario.setTotale(sommario.getTotale() + importo);
                        categoriaEsistente = true;
                        break;
                    }
                }

                // Se la categoria non esiste, aggiungila alla lista
                if (!categoriaEsistente) {
                    sommaCategorie.add(new SommarioCategoria(categoria, importo));
                }
            }
        }

        // Per ogni sommario, esegui una query asincrona per verificare il tipo
        List<SommarioCategoria> categorieFiltrate = new ArrayList<>();

        // Aggiungiamo tutte le operazioni asincrone alla lista di task
        for (SommarioCategoria sommario : sommaCategorie) {
            Task<DocumentSnapshot> task = dbCategoriaRepository.getCategoriaByIdGraph(sommario.getIdCategoria());
            tasks.add(task);

            task.addOnSuccessListener(document -> {
                if (document.exists() && document.contains("tipo")) {
                    boolean tipoCategoria = document.getBoolean("tipo");

                    // Applica il filtro in base al tipo
                    if (tipo == 0 ||
                            (tipo == 1 && tipoCategoria == false) ||
                            (tipo == 2 && tipoCategoria == true)) {
                        categorieFiltrate.add(sommario);  // Aggiungi la categoria filtrata
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Errore nel recupero della categoria", e);
            });
        }

        // Usa Tasks.whenAllSuccess per aspettare che tutte le query siano completate
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(result -> {
            // Dopo che tutte le query sono completate, aggiorna la lista con le categorie filtrate
            sommaCategorieLiveData.setValue(categorieFiltrate);
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Errore nel completamento delle operazioni asincrone", e);
        });
    }

    public void filtraTransazioniPerCategoriaSelezionata(String categoria, Timestamp dataInizio, Timestamp dataFine) {
        List<Transazione> transazioniFiltrate = new ArrayList<>();

        for (Transazione transazione : transazioni) {
            Timestamp data;
            data = transazione.getData();
            if (transazione.getIdCategoria().equalsIgnoreCase(categoria) && data.compareTo(dataInizio) >= 0 && data.compareTo(dataFine)<= 0) {
                transazioniFiltrate.add(transazione);
            }
        }

        // Impostiamo il LiveData per le transazioni filtrate per categoria
        transazioniFiltrateLiveData.setValue(transazioniFiltrate);
    }

    // Getter per le transazioni
    public LiveData<List<Transazione>> getTransazioniLiveData() {
        return transazioniLiveData;
    }



    // Getter per il sommario mensile
    public LiveData<List<SommarioMese>> getSommarioLiveData() {
        return sommarioLiveData;
    }

    // Getter per le transazioni filtrate
    public LiveData<List<Transazione>> getTransazioniFiltrateLiveData() {
        return transazioniFiltrateLiveData;
    }

    // Aggiunge una nuova transazione e aggiorna i dati
    public void addTransazione(Transazione transazione) {
        transazioni.add(transazione);
        updateTransazioniLiveData();
    }

    // Imposta l'intera lista di transazioni
    public void setTransazioni(List<Transazione> nuoveTransazioni) {
        if(nuoveTransazioni!=null) {
            transazioni.clear();
            transazioni.addAll(nuoveTransazioni);
            updateTransazioniLiveData();
        }
    }

    // Rimuove una transazione
    public void deleteTransazione(Transazione transazione) {
        transazioni.remove(transazione);
        updateTransazioniLiveData();
    }

    // Aggiorna il LiveData delle transazioni e il sommario
    private void updateTransazioniLiveData() {
        transazioniLiveData.setValue(new ArrayList<>(transazioni));
        sommarioLiveData.setValue(calcolaSommarioMesi(transazioni));
    }

    // Calcola il sommario aggregando le transazioni per mese e anno
    private List<SommarioMese> calcolaSommarioMesi(List<Transazione> transazioni) {
        Map<String, Double> sommarieMesi = new HashMap<>();

        for (Transazione transazione : transazioni) {
            String key = getMeseAnnoFromTransazione(transazione);
            double importo = transazione.getImporto();
            sommarieMesi.put(key, sommarieMesi.getOrDefault(key, 0.0) + importo);
        }

        List<SommarioMese> sommari = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sommarieMesi.entrySet()) {
            String[] meseAnno = entry.getKey().split(" ");
            String nomeMese = meseAnno[0];
            int anno = Integer.parseInt(meseAnno[1]);
            double bilancio = entry.getValue();

            sommari.add(new SommarioMese(nomeMese, anno, bilancio));
        }

        return sommari;
    }

    // Funzione per filtrare le transazioni di un determinato mese e anno
    public void filtraTransazioniPerMese(String nomeMese, int anno) {
        List<Transazione> transazioniFiltrate = new ArrayList<>();

        for (Transazione transazione : transazioni) {
            String meseAnno = getMeseAnnoFromTransazione(transazione);
            if (meseAnno.equals(nomeMese + " " + anno)) {
                transazioniFiltrate.add(transazione);
            }
        }

        transazioniFiltrateLiveData.setValue(transazioniFiltrate);
    }

    // Ottiene il mese e l'anno dalla transazione
    private String getMeseAnnoFromTransazione(Transazione transazione) {
        Date date = transazione.getData().toDate();  // Supponendo che getData() restituisca il timestamp in millisecondi

        // Crea un oggetto Date dal timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Ottieni la data formattata come "MM/yyyy"
        String dataFormattata = sdf.format(date);



        // Separare mese e anno dalla data formattata
        String[] dataParts = dataFormattata.split("/");

        int mese = getNomeMese(Integer.parseInt(dataParts[1]));
        String anno = dataParts[2];

        return mese + " " + anno;
    }

    // Converte il numero del mese nel nome del mese
    private int getNomeMese(int numeroMese) {
        int[] mesi = {
                R.string.gennaio, R.string.febbraio, R.string.marzo,
                R.string.aprile, R.string.maggio, R.string.giugno,
                R.string.luglio, R.string.agosto,
                R.string.settembre,
                R.string.ottobre,
                R.string.novembre,
                R.string.dicembre
        };
        return mesi[numeroMese - 1];
    }
}
