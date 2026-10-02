package com.jm.trackwallet.adapter;

import static com.jm.trackwallet.util.Constants.colorList;
import static com.jm.trackwallet.util.Constants.iconList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.jm.trackwallet.R;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.model.SommarioCategoria;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.ui.home.MainActivity;

import java.util.List;

public class AdapterSommarioCategoria extends RecyclerView.Adapter<AdapterSommarioCategoria.SommarioViewHolder> {
    private List<SommarioCategoria> sommari;
    private AdapterSommarioCategoria.OnItemClickListener listener;
    private Timestamp dataInizio;
    private Timestamp dataFine;

    private Context context;

    public interface OnItemClickListener {
        void onItemClick(String idCategoria, Timestamp dataInizio, Timestamp dataFine);
    }

    public AdapterSommarioCategoria(Context context, List<SommarioCategoria> sommari, OnItemClickListener listener, Timestamp dataInizio, Timestamp dataFine) {
        this.sommari = sommari;
        this.listener = listener;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterSommarioCategoria.SommarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conto, parent, false);
        return new AdapterSommarioCategoria.SommarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSommarioCategoria.SommarioViewHolder holder, int position) {
        SommarioCategoria sommario = sommari.get(position);
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
        dbCategoriaRepository.getNomeCategoriaById(sommario.getIdCategoria(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String nomeCategoria = task.getResult().getString("nome");
                int icona = task.getResult().getLong("icona").intValue();
                int colore = task.getResult().getLong("colore").intValue();
                holder.icon.setColorFilter(colorList.get(colore), PorterDuff.Mode.SRC_IN);
                holder.icon.setImageResource(iconList.get(icona));
                holder.nome.setText(nomeCategoria);
            } else {
                holder.nome.setText("");
            }
        });
        PreferencesManager pm = new PreferencesManager(context);
        double convertedPrice = sommario.getTotale();
        String valuta = String.format("%.2f", convertedPrice) + "€";
        if (!pm.getCurrency().equals("€") && MainActivity.isOnline(context)) {
            convertedPrice *= pm.getExchangeRate();
            valuta = String.format("%.2f", convertedPrice) + pm.getCurrency();
        }
        holder.totale.setText(valuta);
        if (sommario.getTotale() > 0.0) {
            holder.totale.setTextColor(Color.parseColor("#2E7D32"));
        } else if (sommario.getTotale() < 0.0) {
            holder.totale.setTextColor(Color.parseColor("#D32F2F"));
        }
        holder.cardView.setStrokeWidth(3);


        holder.cardView.setOnClickListener(v -> {
            listener.onItemClick(sommario.getIdCategoria(), dataInizio, dataFine);
        });

    }

    @Override
    public int getItemCount() {
        return sommari.size();
    }

    public void updateData(List<SommarioCategoria> newSommari) {
        this.sommari = newSommari;
        notifyDataSetChanged();
    }


    public static class SommarioViewHolder extends RecyclerView.ViewHolder {
        TextView nome, totale;
        ImageView icon;
        MaterialCardView cardView;

        public SommarioViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.nomeContoTextView);
            totale = itemView.findViewById(R.id.saldoContoTextView);
            icon = itemView.findViewById(R.id.iconaContoImageView);
            cardView = itemView.findViewById(R.id.card_conto);
        }


    }
}
