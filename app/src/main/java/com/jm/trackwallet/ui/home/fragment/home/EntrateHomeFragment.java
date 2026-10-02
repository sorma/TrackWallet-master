package com.jm.trackwallet.ui.home.fragment.home;

import static com.jm.trackwallet.util.Constants.colorList;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jm.trackwallet.R;
import com.jm.trackwallet.adapter.AdapterSommarioCategoria;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.SommarioCategoria;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.ui.home.MainActivity;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class EntrateHomeFragment extends Fragment {

    private TextView rangeData;
    private boolean isGiornoClick;
    private boolean isSettimanaClick;
    private String currentDate;
    private String todayDate;
    private String sevenDaysAgo;
    private ImageButton frecciaDestra;
    private ImageButton frecciaSinistra;
    private RecyclerView recyclerView;
    private TransazioneViewModel viewModel;
    private AdapterSommarioCategoria adapter;

    private PieChart chart;

    public EntrateHomeFragment() {
        // Required empty public constructor
    }

    public static SpeseHomeFragment newInstance() {

        return new SpeseHomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransazioneViewModel.class);
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entrate_home, container, false);
        chart = view.findViewById(R.id.pieChart);
        Button giorno = view.findViewById(R.id.giorno);
        Button settimana = view.findViewById(R.id.settimana);
        Button periodo = view.findViewById(R.id.periodo);
        rangeData = view.findViewById(R.id.rangeData);
        frecciaDestra = view.findViewById(R.id.frecciaDestra);
        frecciaSinistra = view.findViewById(R.id.freacciaSinistra);
        recyclerView = view.findViewById(R.id.speseRecyclerView);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        currentDate = sdf.format(calendar.getTime());
        todayDate = currentDate;
        setUpRange();
        settimana.performClick();
        settimana.setPressed(true);

        MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Scegli periodo")
                .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;
            if (startDate != null && endDate != null) {
                String dataInizio = new SimpleDateFormat("dd/MM/yyyy").format(new Date(startDate));
                String dataFine = new SimpleDateFormat("dd/MM/yyyy").format(new Date(endDate));
                String dataRange = dataInizio + " - " + dataFine;
                rangeData.setText(dataRange);

                try {
                    Date parsedDateInizio = sdf.parse(dataInizio);
                    Date parsedDateFine = sdf.parse(dataFine);
                    assert parsedDateInizio != null;
                    Timestamp timestampInizio = new Timestamp(parsedDateInizio);
                    assert parsedDateFine != null;
                    Timestamp timestampFine = new Timestamp(parsedDateFine);
                    inizializzaRecycler(timestampInizio, timestampFine);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        giorno.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingInflatedId")
            @Override
            public void onClick(View v) {

                try {
                    currentDate = todayDate;
                    Date parsedDate = sdf.parse(currentDate);
                    assert parsedDate != null;
                    Timestamp timestamp = new Timestamp(parsedDate);
                    inizializzaRecycler(timestamp, timestamp);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                // Converte Date in Timestamp

                if (!isGiornoClick) {
                    frecciaDestra.setVisibility(View.VISIBLE);
                    frecciaSinistra.setVisibility(View.VISIBLE);
                    rangeData.setText(todayDate);
                    isGiornoClick = true;
                    isSettimanaClick = false;
                    currentDate = todayDate;
                }

            }
        });

        settimana.setOnClickListener(v -> {

            try {

                currentDate = todayDate;
                Date parsedDateFine = sdf.parse(currentDate);
                calendar.setTime(parsedDateFine);
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                Date sevenDaysAgo = calendar.getTime();
                String sevenDaysAgoFormatted = sdf.format(sevenDaysAgo);
                Date parsedDateInizio = sdf.parse(sevenDaysAgoFormatted);
                assert parsedDateInizio != null;
                Timestamp timestampInizio = new Timestamp(parsedDateInizio);
                assert parsedDateFine != null;
                Timestamp timestampFine = new Timestamp(parsedDateFine);
                inizializzaRecycler(timestampInizio, timestampFine);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            if (!isSettimanaClick) {
                frecciaDestra.setVisibility(View.VISIBLE);
                frecciaSinistra.setVisibility(View.VISIBLE);
                currentDate = todayDate;
                Calendar calendar2 = Calendar.getInstance();
                calendar2.add(Calendar.DAY_OF_MONTH, -7);
                sevenDaysAgo = sdf.format(calendar2.getTime());
                String dateWeek = sevenDaysAgo + " - " + currentDate;
                rangeData.setText(dateWeek);
                isSettimanaClick = true;
                isGiornoClick = false;
            }

        });

        periodo.setOnClickListener(v -> {
            frecciaDestra.setVisibility(View.GONE);
            frecciaSinistra.setVisibility(View.GONE);
            dateRangePicker.show(getParentFragmentManager(), dateRangePicker.toString());
            new Handler(Looper.getMainLooper()).post(() -> {
                Dialog dialog = dateRangePicker.getDialog();
                if (dialog != null && dialog.getWindow() != null) {
                    dialog.getWindow().setLayout(
                            (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
            });
            isSettimanaClick = false;
            isGiornoClick = false;
        });

        frecciaSinistra.setOnClickListener(v -> {
            if (isGiornoClick) {
                Date dataOggi;
                try {
                    dataOggi = sdf.parse(currentDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                assert dataOggi != null;
                calendar.setTime(dataOggi);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                Date newDate = calendar.getTime();
                String newDateString = sdf.format(newDate);
                currentDate = newDateString;
                rangeData.setText(newDateString);
                try {
                    Date parsedDate = sdf.parse(newDateString);
                    assert parsedDate != null;
                    Timestamp timestamp = new Timestamp(parsedDate);
                    inizializzaRecycler(timestamp, timestamp);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            if (isSettimanaClick) {
                try {
                    Date dateObj1 = sdf.parse(currentDate);
                    Date dateObj2 = sdf.parse(sevenDaysAgo);
                    Calendar calendar1 = Calendar.getInstance();
                    assert dateObj1 != null;
                    calendar1.setTime(dateObj1);
                    calendar1.add(Calendar.DAY_OF_MONTH, -7);
                    Calendar calendar2 = Calendar.getInstance();
                    assert dateObj2 != null;
                    calendar2.setTime(dateObj2);
                    calendar2.add(Calendar.DAY_OF_MONTH, -7);
                    currentDate = sdf.format(calendar1.getTime());
                    sevenDaysAgo = sdf.format(calendar2.getTime());
                    rangeData.setText(sevenDaysAgo + " - " + currentDate);

                    Date parsedDateInizio = sdf.parse(sevenDaysAgo);
                    Date parsedDateFine = sdf.parse(currentDate);
                    assert parsedDateInizio != null;
                    Timestamp timestampInizio = new Timestamp(parsedDateInizio);
                    assert parsedDateFine != null;
                    Timestamp timestampFine = new Timestamp(parsedDateFine);
                    inizializzaRecycler(timestampInizio, timestampFine);


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        frecciaDestra.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingInflatedId")
            @Override
            public void onClick(View v) {
                if (isGiornoClick) {
                    Calendar oggi = Calendar.getInstance();
                    oggi.set(Calendar.HOUR_OF_DAY, 0);
                    oggi.set(Calendar.MINUTE, 0);
                    oggi.set(Calendar.SECOND, 0);
                    oggi.set(Calendar.MILLISECOND, 0);
                    Date dataCorrente;
                    try {
                        dataCorrente = sdf.parse(currentDate);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    assert dataCorrente != null;
                    if (dataCorrente.before(oggi.getTime())) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dataCorrente);
                        calendar.add(Calendar.DAY_OF_MONTH, +1);
                        Date newDate = calendar.getTime();
                        currentDate = sdf.format(newDate);
                        rangeData.setText(currentDate);
                    }

                    try {
                        Date parsedDate = sdf.parse(currentDate);
                        assert parsedDate != null;
                        Timestamp timestamp = new Timestamp(parsedDate);
                        inizializzaRecycler(timestamp, timestamp);

                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (isSettimanaClick) {
                    try {
                        Date dateObj1 = sdf.parse(currentDate);
                        Date dateObj2 = sdf.parse(sevenDaysAgo);

                        Calendar oggi = Calendar.getInstance();
                        oggi.set(Calendar.HOUR_OF_DAY, 0);
                        oggi.set(Calendar.MINUTE, 0);
                        oggi.set(Calendar.SECOND, 0);
                        oggi.set(Calendar.MILLISECOND, 0);

                        Calendar calendar1 = Calendar.getInstance();
                        assert dateObj1 != null;
                        calendar1.setTime(dateObj1);

                        Calendar calendar2 = Calendar.getInstance();
                        assert dateObj2 != null;
                        calendar2.setTime(dateObj2);
                        if (calendar1.before(oggi)) {
                            calendar1.add(Calendar.DAY_OF_MONTH, 7);
                            calendar2.add(Calendar.DAY_OF_MONTH, 7);
                            currentDate = sdf.format(calendar1.getTime());
                            sevenDaysAgo = sdf.format(calendar2.getTime());
                        }
                        rangeData.setText(sevenDaysAgo + " - " + currentDate);

                        Date parsedDateInizio = sdf.parse(sevenDaysAgo);
                        Date parsedDateFine = sdf.parse(currentDate);
                        assert parsedDateInizio != null;
                        Timestamp timestampInizio = new Timestamp(parsedDateInizio);
                        assert parsedDateFine != null;
                        Timestamp timestampFine = new Timestamp(parsedDateFine);
                        inizializzaRecycler(timestampInizio, timestampFine);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return view;
    }

    private void setUpRange() {
        frecciaDestra.setVisibility(View.VISIBLE);
        frecciaSinistra.setVisibility(View.VISIBLE);
        currentDate = todayDate;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.DAY_OF_MONTH, -7);
        sevenDaysAgo = sdf.format(calendar2.getTime());
        String dateWeek = sevenDaysAgo + " - " + currentDate;
        rangeData.setText(dateWeek);
        isSettimanaClick = true;
        isGiornoClick = false;

        Date parsedDateInizio;
        try {
            parsedDateInizio = sdf.parse(sevenDaysAgo);
            Date parsedDateFine = sdf.parse(currentDate);
            assert parsedDateInizio != null;
            Timestamp timestampInizio = new Timestamp(parsedDateInizio);
            assert parsedDateFine != null;
            Timestamp timestampFine = new Timestamp(parsedDateFine);
            inizializzaRecycler(timestampInizio, timestampFine);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void inizializzaRecycler(Timestamp dataInizio, Timestamp dataFine) {
        viewModel.filtraCategorieConImportoTotale(dataInizio, dataFine, 2);
        chart.clear();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterSommarioCategoria(getContext(), new ArrayList<>(), (idCategoria, dataInizio1, dataFine1) -> {
            viewModel.filtraTransazioniPerCategoriaSelezionata(idCategoria, dataInizio1, dataFine1);

            NavController navController = Navigation.findNavController(requireView());
            Bundle bundle = new Bundle();
            bundle.putString("idCategoria", idCategoria);
            bundle.putParcelable("dataInizio", dataInizio1);
            bundle.putParcelable("dataFine", dataFine1);
            navController.navigate(R.id.action_homeFragment_to_transazioniSommarioCategoria, bundle);
        }, dataInizio, dataFine);


        recyclerView.setAdapter(adapter);

        DBTransazioneRepository dbTransazioneRepository = new DBTransazioneRepository();
        dbTransazioneRepository.getTransazioniByUtente(FirebaseManager.getInstance().getCurrentUser().getUid()).observe(getViewLifecycleOwner(), transazioni -> {
            viewModel.setTransazioni(transazioni);
            viewModel.filtraCategorieConImportoTotale(dataInizio, dataFine, 2);
        });

        viewModel.getSommaCategorieLiveData().observe(getViewLifecycleOwner(), sommari -> {

            sommari.sort(Comparator.comparingDouble(SommarioCategoria::getTotale).reversed());
            float itemHeight = getResources().getDimension(R.dimen.card);
            int recyclerViewHeight = (int) (sommari.size() * itemHeight);
            recyclerView.setMinimumHeight(recyclerViewHeight);
            grafico(sommari);
            adapter.updateData(sommari);
        });
    }

    public void grafico(List<SommarioCategoria> sommari) {
        if (sommari.size() > 0) {
            chart.setVisibility(View.VISIBLE);
            chart.setExtraOffsets(40f, 0f, 40f, 0f);

            DBCategoriaRepository categoriaRepository = new DBCategoriaRepository();

            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            final double[] somma = {0.0};

            // Mappa per tenere traccia dell'ordine corretto dei colori
            Map<Integer, Integer> colorMap = new HashMap<>();

            int index = 0;
            for (SommarioCategoria item : sommari) {
                if (item.getTotale() < 0)
                    entries.add(new PieEntry((float) -item.getTotale()));
                else
                    entries.add(new PieEntry((float) item.getTotale()));

                somma[0] += item.getTotale();
                int currentIndex = index; // Memorizza l'indice corrente

                Task<DocumentSnapshot> task = categoriaRepository.getCategoriaByIdGraph(item.getIdCategoria());
                tasks.add(task);

                task.addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("colore")) {
                        int colore = Objects.requireNonNull(document.getLong("colore")).intValue();
                        // Salviamo il colore nella posizione corretta
                        colorMap.put(currentIndex, colorList.get(colore));
                    }
                });

                index++;
            }

            // Aspettiamo tutte le richieste e riordiniamo la lista dei colori
            Tasks.whenAllSuccess(tasks).addOnSuccessListener(result -> {
                for (int i = 0; i < sommari.size(); i++) {
                    colors.add(colorMap.getOrDefault(i, Color.GRAY)); // Se manca un colore, mettiamo un valore di default
                }
                aggiornaGrafico(entries, colors, somma[0]);
            }).addOnFailureListener(e -> Log.e("Firebase", "Errore nel recupero dati", e));

        } else {
            chart.setVisibility(View.GONE);
        }
    }

    public void aggiornaGrafico(List<PieEntry> entries, List<Integer> colors, double somma) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColors(colors);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);


        dataSet.setValueLinePart1Length(0.6f);
        dataSet.setValueLinePart2Length(0.3f);
        dataSet.setValueLineWidth(2f);
        dataSet.setValueLinePart1OffsetPercentage(115f); // La linea inizia al di fuori del grafico
        dataSet.setUsingSliceColorAsValueLineColor(true);

// Configurazione dell'aspetto dei valori
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

// Formattazione dei valori come percentuali
        dataSet.setValueFormatter(new ValueFormatter() {
            private final NumberFormat formatter = NumberFormat.getPercentInstance();

            @Override
            public String getFormattedValue(float value) {
                return formatter.format(value / 100f);
            }
        });
        chart.setUsePercentValues(true);

        dataSet.setSelectionShift(3f);

// Configurazione del buco centrale
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(70f);
        chart.setTransparentCircleRadius(60f);
        chart.setHoleColor(Color.TRANSPARENT);
//dimensione ciambella


// Testo centrale
        chart.setDrawCenterText(true);
        chart.setCenterTextSize(20f);
        chart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        chart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.md_theme_onSurfaceVariant));

        PreferencesManager pm = new PreferencesManager(getContext());
        double convertedPrice = somma;
        String valuta = String.format("%.2f", convertedPrice) + "€";
        if (!pm.getCurrency().equals("€") && MainActivity.isOnline(getContext())) {
            convertedPrice *= pm.getExchangeRate();
            valuta = String.format("%.2f", convertedPrice) + pm.getCurrency();
        }
        chart.setCenterText(valuta);

// Disabilita la legenda e la descrizione
        chart.getLegend().setEnabled(false);
        chart.setDescription(null);

// Imposta i dati del grafico
        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate();

    }
}