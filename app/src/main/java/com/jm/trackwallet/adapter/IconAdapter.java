package com.jm.trackwallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jm.trackwallet.R;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {
    private List<Integer> icone;
    private int selectedIcon = -1;
    private Context context;

    public IconAdapter(List<Integer> icone, Context context) {
        this.icone = icone;
        this.context=context;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating il layout item_icon.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icona, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        // Settaggio dell'icona
        holder.icona.setImageResource(icone.get(position));

        // Gestire la selezione: Se la posizione è quella selezionata, evidenzia l'icona
        holder.icona.setSelected(position == selectedIcon); // Seleziona l'icona se è quella attiva
        // Gestire la selezione: Se la posizione è quella selezionata, evidenzia l'icona
        int color = (position == selectedIcon) ?
                ContextCompat.getColor(context, R.color.md_theme_primary) : // Colore primario se selezionata
                ContextCompat.getColor(context, R.color.md_theme_outline); // Colore di default

        holder.icona.setColorFilter(color);
        // Impostazione del listener per il click
        holder.itemView.setOnClickListener(v -> {
            selectedIcon = position; // Memorizza l'icona selezionata
            notifyDataSetChanged(); // Notifica il cambiamento a RecyclerView
        });
    }

    @Override
    public int getItemCount() {
        return icone.size(); // Ritorna il numero totale delle icone
    }

    public static class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView icona;

        public IconViewHolder(View itemView) {
            super(itemView);
            icona = itemView.findViewById(R.id.icona); // Assicurati che l'ID corrisponda al layout
        }
    }

    // Restituisci l'indice dell'icona selezionata
    public int getSelectedIcon() {
        return selectedIcon;
    }
}


