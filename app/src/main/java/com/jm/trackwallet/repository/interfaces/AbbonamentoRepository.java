package com.jm.trackwallet.repository.interfaces;

import androidx.lifecycle.LiveData;

import com.jm.trackwallet.model.Abbonamento;

import java.util.List;

public interface AbbonamentoRepository {
    String collectionName = "abbonamenti";

    LiveData<List<Abbonamento>> getAbbonamentiByUserId(String idUtente);

    void addAbbonamento(Abbonamento abbonamento);

    void deleteAbbonamento(String idAbbonamento);
}
