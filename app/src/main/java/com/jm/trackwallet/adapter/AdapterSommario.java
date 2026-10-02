package com.jm.trackwallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.jm.trackwallet.R;
import com.jm.trackwallet.model.SommarioMese;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.ui.home.MainActivity;

import java.util.List;

public class AdapterSommario extends RecyclerView.Adapter<AdapterSommario.SommarioViewHolder> {

    private List<SommarioMese> sommari;
    private OnItemClickListener listener;

    private Context context;

    public interface OnItemClickListener {
        void onItemClick(SommarioMese sommario);
    }

    public AdapterSommario(List<SommarioMese> sommari, OnItemClickListener listener, Context context) {
        this.sommari = sommari;
        this.listener = listener;
        this.context=context;
    }

    @NonNull
    @Override
    public SommarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sommario, parent, false);
        return new SommarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SommarioViewHolder holder, int position) {
        SommarioMese sommario = sommari.get(position);
        int mese = Integer.parseInt(sommario.getNomeMese());
        holder.meseAnno.setText(context.getString(mese) + " " + sommario.getAnno());
        PreferencesManager pm = new PreferencesManager(context);
        double convertedPrice = sommario.getBilancio();
        String bilancio= String.format("%.2f", convertedPrice) + "€";
        if (!pm.getCurrency().equals("€") && MainActivity.isOnline(context)){
            convertedPrice *= pm.getExchangeRate();
            bilancio = String.format("%.2f", convertedPrice) + pm.getCurrency();
        }
        holder.bilancio.setText(bilancio);
        if (sommario.getBilancio() > 0) {
            holder.bilancio.setTextColor(Color.parseColor("#2E7D32"));
        } else if (sommario.getBilancio() < 0) {
            holder.bilancio.setTextColor(Color.parseColor("#D32F2F"));
        }
        holder.cardView.setStrokeWidth(3);


        holder.cardView.setOnClickListener(v -> {
            listener.onItemClick(sommario);
        });

    }

    @Override
    public int getItemCount() {
        return sommari.size();
    }

    public void updateData(List<SommarioMese> newSommari) {
        this.sommari = newSommari;
        notifyDataSetChanged();
    }


    public static class SommarioViewHolder extends RecyclerView.ViewHolder {
        TextView meseAnno, bilancio;
        MaterialCardView cardView;

        public SommarioViewHolder(View itemView) {
            super(itemView);
            meseAnno = itemView.findViewById(R.id.meseAnnoTextView);
            bilancio = itemView.findViewById(R.id.bilancioTextView);
            cardView = itemView.findViewById(R.id.cardView);
        }


    }

}