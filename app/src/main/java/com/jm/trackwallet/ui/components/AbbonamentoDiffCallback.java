package com.jm.trackwallet.ui.components;

import androidx.recyclerview.widget.DiffUtil;

import com.jm.trackwallet.model.Abbonamento;

import java.util.List;

public class AbbonamentoDiffCallback extends DiffUtil.Callback {
    private final List<Abbonamento> oldList;
    private final List<Abbonamento> newList;

    public AbbonamentoDiffCallback(List<Abbonamento> oldList, List<Abbonamento> newList) {
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
        return oldList.get(oldItemPosition).getIdAbbonamento().equals(newList.get(newItemPosition).getIdAbbonamento());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Abbonamento oldAbbonamento = oldList.get(oldItemPosition);
        Abbonamento newAbbonamento = newList.get(newItemPosition);
        return oldAbbonamento.getIdAbbonamento().equals(newAbbonamento.getIdAbbonamento());
    }
}
