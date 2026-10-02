package com.jm.trackwallet.ui.components;

import androidx.recyclerview.widget.DiffUtil;

import com.jm.trackwallet.model.Categoria;

import java.util.List;

public class CategoriaDiffCallback extends DiffUtil.Callback {
    private final List<Categoria> oldList;
    private final List<Categoria> newList;

    public CategoriaDiffCallback(List<Categoria> oldList, List<Categoria> newList) {
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
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}