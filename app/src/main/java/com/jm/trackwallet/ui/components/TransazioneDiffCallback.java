package com.jm.trackwallet.ui.components;

import androidx.recyclerview.widget.DiffUtil;

import com.jm.trackwallet.model.Transazione;

import java.util.List;

public class TransazioneDiffCallback extends DiffUtil.Callback {

    private final List<Transazione> oldList;
    private final List<Transazione> newList;

    public TransazioneDiffCallback(List<Transazione> oldList, List<Transazione> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getIdCategoria().equals(newList.get(newItemPosition).getIdCategoria());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Transazione oldTransazione = oldList.get(oldItemPosition);
        Transazione newTransazione = newList.get(newItemPosition);

        // Confronta i campi rilevanti della transazione
        return oldTransazione.getTitolo().equals(newTransazione.getTitolo()) &&
                oldTransazione.getImporto() == newTransazione.getImporto() &&
                oldTransazione.getData().equals(newTransazione.getData()) &&
                oldTransazione.getIdCategoria().equals(newTransazione.getIdCategoria());
    }
}

