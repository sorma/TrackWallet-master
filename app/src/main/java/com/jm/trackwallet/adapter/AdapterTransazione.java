package com.jm.trackwallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jm.trackwallet.R;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.ui.home.MainActivity;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModel;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelCategoria;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelConto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdapterTransazione extends RecyclerView.Adapter<AdapterTransazione.TransazioneViewHolder> {

    private List<Transazione> transazioni;
    private Context context;

    // Costruttore del ListAdapter
    public AdapterTransazione(Context context, List<Transazione> transazioni) {
        this.context = context;
        this.transazioni = transazioni;
    }

    @NonNull
    @Override
    public TransazioneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating la view dalla risorsa layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transazione, parent, false);
        return new TransazioneViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterTransazione.TransazioneViewHolder holder, int position) {
        Transazione transazione = transazioni.get(position);
        // Effetto visivo per il click (come se fosse un bottone premuto)


        holder.titoloTransazione.setText(transazione.getTitolo());
        if (transazione.getImporto() < 0) {
            holder.importoTransazione.setTextColor(Color.parseColor("#D32F2F"));
        } else if (transazione.getImporto() > 0) {
            holder.importoTransazione.setTextColor(Color.parseColor("#2E7D32"));
        }

        PreferencesManager pm = new PreferencesManager(context);
        double convertedPrice = transazione.getImporto();
        String valuta = String.format("%.2f", convertedPrice) + "€";
        if (!pm.getCurrency().equals("€") && MainActivity.isOnline(context)) {
            convertedPrice *= pm.getExchangeRate();
            valuta = String.format("%.2f", convertedPrice) + pm.getCurrency();
        }
        holder.importoTransazione.setText(valuta);
        Date data = transazione.getData().toDate(); // Supponiamo che restituisca un java.util.Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dataString = sdf.format(data);
        holder.dataTransazione.setText(dataString);
        DBContoRepository dbContoRepository = new DBContoRepository();
        dbContoRepository.getNomeContoById(transazione.getIdConto(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String nomeConto = task.getResult().getString("nome");
                holder.contoTransazione.setText(nomeConto);
            } else {
                holder.contoTransazione.setText("");
            }
        });
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
        dbCategoriaRepository.getNomeCategoriaById(transazione.getIdCategoria(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String nomeCategoria = task.getResult().getString("nome");
                holder.categoriaTransazione.setText(nomeCategoria);
            } else {
                holder.categoriaTransazione.setText("");
            }
        });
        holder.cardView.setOnLongClickListener(view -> {
            showDeleteConfirmationDialog(transazione);
            return true;
        });
    }

    private void showDeleteConfirmationDialog(Transazione transazione) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.conferma_cancellazione);
        builder.setMessage(R.string.text_sicuro_eliminazione_transazione);

        builder.setPositiveButton(R.string.elimina, (dialog, which) -> {

            deleteTransazione(transazione);
        });
        builder.setNegativeButton(R.string.annulla, (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void deleteTransazione(Transazione transazione) {
        DBTransazioneRepository dbTransazioneRepository = new DBTransazioneRepository();
        dbTransazioneRepository.deleteTransazione(transazione);

        TransazioneViewModelCategoria transazioneViewModelCategoria = new ViewModelProvider((FragmentActivity) context).get(TransazioneViewModelCategoria.class);
        transazioneViewModelCategoria.deleteTransazione(transazione);

        TransazioneViewModelConto transazioneViewModelConto = new ViewModelProvider((FragmentActivity) context).get(TransazioneViewModelConto.class);
        transazioneViewModelConto.deleteTransazione(transazione);

        TransazioneViewModel transazioneViewModel = new ViewModelProvider((FragmentActivity) context).get(TransazioneViewModel.class);
        transazioneViewModel.deleteTransazione(transazione);

        String conto = transazione.getIdConto();
        double imp = -transazione.getImporto();

        DBContoRepository dbContoRepository = new DBContoRepository();
        dbContoRepository.updateSaldo(conto, imp);
    }


    // ViewHolder per la Transazione
    public static class TransazioneViewHolder extends RecyclerView.ViewHolder {
        TextView titoloTransazione;
        TextView importoTransazione;
        TextView dataTransazione;
        TextView categoriaTransazione;
        TextView contoTransazione;

        MaterialCardView cardView;

        public TransazioneViewHolder(View itemView) {
            super(itemView);
            // Inizializziamo i riferimenti delle TextView
            titoloTransazione = itemView.findViewById(R.id.titoloTransazione);
            importoTransazione = itemView.findViewById(R.id.importoTransazione);
            dataTransazione = itemView.findViewById(R.id.dataTransazione);
            cardView = itemView.findViewById(R.id.cardView);
            categoriaTransazione = itemView.findViewById(R.id.categoriaTransazione);
            contoTransazione = itemView.findViewById(R.id.contoTransazione);
        }
    }

    public int getItemCount() {
        return transazioni.size();
    }


}
