package com.jm.trackwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jm.trackwallet.R;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final int[] icons;

    public CustomSpinnerAdapter(Context context, String[] values, int[] icons) {
        super(context, R.layout.item_spinner, values); // Passa il layout del tuo spinner item
        this.context = context;
        this.values = values;
        this.icons = icons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Usa questo metodo per il testo da visualizzare nella selezione
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_spinner, parent, false);
        }

        // Riferimenti agli oggetti nella vista
        ImageView icon = view.findViewById(R.id.icon);
        TextView text = view.findViewById(R.id.text);

        // Imposta l'icona e il testo
        icon.setImageResource(icons[position]);
        text.setText(values[position]);

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Usa lo stesso layout anche per il dropdown
        return getView(position, convertView, parent);
    }
}

