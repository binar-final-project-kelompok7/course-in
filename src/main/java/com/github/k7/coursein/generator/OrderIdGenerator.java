package com.github.k7.coursein.generator;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
public class OrderIdGenerator {

    private final AtomicLong orderCounter = new AtomicLong(1);

    private LocalDate lastUpdatedDate = LocalDate.now();

    public String generateOrderId() {
        resetCounterIfDayIsChange();

        long orderNumber = orderCounter.getAndIncrement();
        String formattedOrderNumber = String.format("%06d", orderNumber);

        return String.format("INV-%d%02d%02d-%s",
            lastUpdatedDate.getYear(),
            lastUpdatedDate.getMonthValue(),
            lastUpdatedDate.getDayOfMonth(),
            formattedOrderNumber);
    }

    private void resetCounterIfDayIsChange() {
        boolean isYesterday = lastUpdatedDate.isBefore(LocalDate.now());
        if (isYesterday) {
            orderCounter.set(1);
            lastUpdatedDate = LocalDate.now();
        }
    }

}
