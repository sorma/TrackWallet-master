package com.jm.trackwallet.adapter;

import static com.jm.trackwallet.util.Constants.colorList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jm.trackwallet.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {
    private List<Integer> colori;
    private int selectedColor = -1;
    private Context context;

    public ColorAdapter(List<Integer> colori, Context context) {
        this.colori = colori;
        this.context=context;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating il layout item_color.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_colore, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Impostiamo il colore dell'elemento in base alla posizione
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);  // Forma ovale
        drawable.setColor(colorList.get(position));
        drawable.setStroke(3, ContextCompat.getColor(context,R.color.md_theme_outline));// Colore di sfondo dell'elemento
        holder.spunta.setColorFilter(ContextCompat.getColor(context, R.color.md_theme_background), PorterDuff.Mode.SRC_IN);

        if(position==selectedColor){
            holder.spunta.setVisibility(View.VISIBLE);
        }else{
            holder.spunta.setVisibility(View.GONE);
        }

        holder.colore.setBackground(drawable);
        // Imposta il background della View

        // Gestire la selezione: se la posizione corrisponde al colore selezionato, evidenzia l'elemento
        holder.colore.setSelected(position == selectedColor); // Seleziona il colore se è quello attivo

        // Impostiamo un listener per il click
        holder.colore.setOnClickListener(v -> {
            selectedColor = position; // Memorizza il colore selezionato
            notifyDataSetChanged(); // Notifica il cambiamento alla RecyclerView
        });
    }

    @Override
    public int getItemCount() {
        return colori.size(); // Ritorna il numero totale di colori
    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder {
        FrameLayout colore;
        ImageView spunta;

        public ColorViewHolder(View itemView) {
            super(itemView);
            colore = itemView.findViewById(R.id.colore); // Assicurati che l'ID corrisponda al layout
            spunta = itemView.findViewById(R.id.spunta);
        }
    }

    // Restituisci l'indice del colore selezionato
    public int getSelectedColor() {
        return selectedColor;
    }
}
