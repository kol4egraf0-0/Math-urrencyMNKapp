package com.example.mnkvalutiapp.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mnkvalutiapp.HelperChart.LinearRegression;
import com.example.mnkvalutiapp.HelperCurrency.CurrencyApiService;
import com.example.mnkvalutiapp.HelperCurrency.ExchangeRateResponse;
import com.example.mnkvalutiapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private LineChart lineChart;
    private CurrencyApiService apiService;
    private List<Double> historicalRates = new ArrayList<>();
    private Handler handler = new Handler();

    private int tickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = findViewById(R.id.lineChart);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.cbr-xml-daily.ru/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(CurrencyApiService.class);

        fetchExchangeRates();

        handler.post(updateTask);
    }

    private final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                tickCount++;

                if(tickCount%12==0)
                {
                    fetchExchangeRates();
                }
                else {
                    predictNextRate();
                }

                handler.postDelayed(this, 5000);
            }
        };


    private void fetchExchangeRates() {
        apiService.getExchangeRates().enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if(response.isSuccessful() && response.body()!=null)
                {
                    double currentRate = response.body().Valute.get("USD").Value;
                    if (historicalRates.size() >= 10) {
                        historicalRates.remove(0);
                    }
                    historicalRates.add(currentRate);

                    Log.d("CHART", "Добавляем курс: " + currentRate + " (размер списка: " + historicalRates.size() + ")");

                    if (historicalRates.size() >= 2) {
                        showChart();
                    }

                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                Log.e("API", "Ошибка загрузки данных", t);
            }
        });
    }
    private void predictNextRate() {
        if (historicalRates.size() < 2) {
            return;
        }

        // массивы X и Y для линейной регрессии
        double[] x = new double[historicalRates.size()];
        double[] y = new double[historicalRates.size()];
        for (int i = 0; i < historicalRates.size(); i++) {
            x[i] = i;
            y[i] = historicalRates.get(i);
        }

        LinearRegression regression = new LinearRegression(x, y);

        double futureX = x[x.length - 1] + 1;
        double predictedRate = regression.predict(futureX);

        double noise = (Math.random() - 0.01) * 0.01 * predictedRate; // ±1%
        predictedRate += noise;

        historicalRates.add(predictedRate);

        if (historicalRates.size() > 10) {
            historicalRates.remove(0);
        }

        Log.d("CHART", "Прогнозируем курс: " + predictedRate + " (размер списка: " + historicalRates.size() + ")");
        showChart();
    }

    private void showChart() {
        List<Entry> entries = new ArrayList<>();
        double[] x = new double[historicalRates.size()];
        double[] y = new double[historicalRates.size()];

        for (int i = 0; i < historicalRates.size(); i++) {
            x[i] = i;
            y[i] = historicalRates.get(i);
            entries.add(new Entry((float) x[i], (float) y[i]));
        }

        LinearRegression regression = new LinearRegression(x, y);

        for (int i = 1; i <= 5; i++) {
            double futureX = x[x.length - 1] + i;
            double futureY = regression.predict(futureX);
            entries.add(new Entry((float) futureX, (float) futureY));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Курс USD");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChart.clear();
        lineChart.setData(lineData);
        lineChart.invalidate();

        Description desc = new Description();
        desc.setText("Прогноз курса USD");
        lineChart.setDescription(desc);
    }
}