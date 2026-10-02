package com.jm.trackwallet.ui.components;

import androidx.recyclerview.widget.DiffUtil;

import com.jm.trackwallet.model.Conto;

import java.util.List;

public class ContoDiffCallback extends DiffUtil.Callback {
    private final List<Conto> oldList;
    private final List<Conto> newList;

    public ContoDiffCallback(List<Conto> oldList, List<Conto> newList) {
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
        // Verifica che i conti abbiano lo stesso ID
        return oldList.get(oldItemPosition).getIdConto().equals(newList.get(newItemPosition).getIdConto());
    }
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Verifica che i contenuti dei conti siano uguali
        Conto oldConto = oldList.get(oldItemPosition);
        Conto newConto = newList.get(newItemPosition);

        // Puoi anche controllare singoli campi come il saldo o altri dettagli, se necessario
        return oldConto.getSaldo() == newConto.getSaldo() && oldConto.getNome().equals(newConto.getNome());
    }
}
