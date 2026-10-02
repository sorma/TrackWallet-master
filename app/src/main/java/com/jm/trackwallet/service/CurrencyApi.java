package com.jm.trackwallet.service;

import com.jm.trackwallet.model.ExchangeRateResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CurrencyApi {
    @GET("v6/{apiKey}/latest/EUR") // Usa il parametro apiKey qui
    Call<ExchangeRateResponse> getExchangeRates(@Path("apiKey") String apiKey);
}

