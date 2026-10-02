package com.jm.trackwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jm.trackwallet.R;
import com.jm.trackwallet.repository.DBAbbonamentoRepository;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.model.Abbonamento;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.ui.home.MainActivity;
import com.jm.trackwallet.ui.components.AbbonamentoDiffCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterAbbonamento extends RecyclerView.Adapter<AdapterAbbonamento.AbbonamentoViewHolder> {
    private List<Abbonamento> abbonamenti;
    private Context context;

    public AdapterAbbonamento(Context context, List<Abbonamento> abbonamenti) {
        this.context = context;
        this.abbonamenti = abbonamenti;
    }

    @NonNull
    @Override
    public AdapterAbbonamento.AbbonamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_abbonamento, parent, false);
        return new AdapterAbbonamento.AbbonamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterAbbonamento.AbbonamentoViewHolder holder, int position) {
        Abbonamento abbonamento = abbonamenti.get(position);
        holder.nomeAbbonamentoTextView.setText(abbonamento.getNome());
        PreferencesManager pm = new PreferencesManager(context);
        double convertedPrice = abbonamento.getImporto();
        String valuta = String.format("%.2f", convertedPrice) + "€";
        if (!pm.getCurrency().equals("€") && MainActivity.isOnline(context)) {
            convertedPrice *= pm.getExchangeRate();
            valuta = String.format("%.2f", convertedPrice) + pm.getCurrency();
        }
        holder.importoTextView.setText(valuta);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Formattazione
        String dataInizio = sdf.format(abbonamento.getDataInizio().toDate());
        String dataFine = sdf.format(abbonamento.getDataFine().toDate());
        holder.dataInizioTextView.setText(dataInizio);
        holder.dataFineTextView.setText(dataFine);

        DBContoRepository dbContoRepository=new DBContoRepository();
        dbContoRepository.getNomeContoById(abbonamento.getConto(),task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String nomeConto = task.getResult().getString("nome");
                holder.contoAbbonamento.setText(nomeConto);
            } else {
                holder.contoAbbonamento.setText("");
            }
        } );
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
        dbCategoriaRepository.getNomeCategoriaById(abbonamento.getCategoria(),task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String nomeCategoria = task.getResult().getString("nome");
                holder.categoriaAbbonamento.setText(nomeCategoria);
            } else {
                holder.categoriaAbbonamento.setText("");
            }
        });

        holder.cardView.setOnLongClickListener(view -> {
            showDeleteConfirmationDialog(abbonamento);
            return true;
        });
    }

    private void showDeleteConfirmationDialog(Abbonamento abbonamento) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.conferma_cancellazione);
        builder.setMessage(R.string.text_sei_sicuro_eliminazione_abbonamento);

        builder.setPositiveButton(R.string.elimina, (dialog, which) -> {
            deleteAbbonamento(abbonamento);
        });
        builder.setNegativeButton(R.string.annulla, (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void deleteAbbonamento(Abbonamento abbonamento) {
        List<Abbonamento> nuovaLista = new ArrayList<>(abbonamenti);
        nuovaLista.remove(abbonamento);

        AbbonamentoDiffCallback diffCallback = new AbbonamentoDiffCallback(abbonamenti, nuovaLista);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        abbonamenti.clear();
        abbonamenti.addAll(nuovaLista);
        diffResult.dispatchUpdatesTo(this);

        DBAbbonamentoRepository dbAbbonamentoRepository = new DBAbbonamentoRepository();
        dbAbbonamentoRepository.deleteAbbonamento(abbonamento.getIdAbbonamento());
    }

    @Override
    public int getItemCount() {
        return abbonamenti.size();
    }

    public static class AbbonamentoViewHolder extends RecyclerView.ViewHolder {
        TextView nomeAbbonamentoTextView, importoTextView, dataInizioTextView, dataFineTextView, contoAbbonamento, categoriaAbbonamento;
        MaterialCardView cardView;

        public AbbonamentoViewHolder(View itemView) {
            super(itemView);
            nomeAbbonamentoTextView = itemView.findViewById(R.id.nomeAbbonamentoTextView);
            importoTextView = itemView.findViewById(R.id.importoAbbonamentoTextView);
            cardView = itemView.findViewById(R.id.cardAbbonamentoView);
            dataInizioTextView = itemView.findViewById(R.id.dataInizioTextView);
            dataFineTextView = itemView.findViewById(R.id.dataFineTextView);
            contoAbbonamento = itemView.findViewById(R.id.contoAbbonamentoTextView);
            categoriaAbbonamento = itemView.findViewById(R.id.categoriaAbbonamentoTextView);
        }
    }
}
