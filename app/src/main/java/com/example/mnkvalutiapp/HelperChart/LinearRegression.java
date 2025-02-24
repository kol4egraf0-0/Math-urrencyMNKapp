package com.example.mnkvalutiapp.HelperChart;

public class LinearRegression {
    private final double slope;  // Коэффициент наклона
    private final double intercept;  // Свободный член
    public LinearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }

        // Вычисляем коэффициенты уравнения y = ax + b
        slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        intercept = (sumY - slope * sumX) / n;
    }
    public double predict(double x) {
        return slope * x + intercept;
    }
}
