package com.vinncorp.erp.modules.projects.engine;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;

@Component
public class MonteCarloSimulator {

    private static final int DEFAULT_ITERATIONS = 1000;
    private final Random random = new Random();

    public SimulationResult simulate(double remainingPoints, double meanVelocity, double velocityStdDev,
                                     LocalDate startDate, int daysRemaining) {
        if (remainingPoints <= 0) {
            return new SimulationResult(startDate, startDate, startDate, 0, 1.0);
        }
        double safeMean = Math.max(0.5, meanVelocity);
        double safeStd = Math.max(0.1, velocityStdDev);
        LocalDate[] completionDates = new LocalDate[DEFAULT_ITERATIONS];

        for (int i = 0; i < DEFAULT_ITERATIONS; i++) {
            double left = remainingPoints;
            int day = 0;
            while (left > 0 && day < Math.max(daysRemaining * 3, 90)) {
                double daily = Math.max(0, safeMean + random.nextGaussian() * safeStd);
                left -= daily;
                day++;
            }
            completionDates[i] = startDate.plusDays(day);
        }

        Arrays.sort(completionDates);
        int p50 = (int) (DEFAULT_ITERATIONS * 0.50);
        int p85 = (int) (DEFAULT_ITERATIONS * 0.85);
        int p95 = (int) (DEFAULT_ITERATIONS * 0.95);
        double confidence = Math.min(0.95, safeMean / (safeMean + safeStd));

        return new SimulationResult(
                completionDates[p50],
                completionDates[p85],
                completionDates[p95],
                remainingPoints / safeMean,
                confidence
        );
    }

    public record SimulationResult(
            LocalDate p50,
            LocalDate p85,
            LocalDate p95,
            double meanRemainingPoints,
            double confidenceScore
    ) {}
}



