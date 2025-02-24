package com.example.mnkvalutiapp.HelperCurrency;
import retrofit2.Call;
import retrofit2.http.GET;
public interface CurrencyApiService {
    @GET("daily_json.js")
    Call<ExchangeRateResponse> getExchangeRates();
}
