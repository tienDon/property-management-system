package com.pms.propertymanagement.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Parse ISO datetime string và format lại
     */
    public static String formatDateTime(String isoDateTime) {
        LocalDateTime dateTime = LocalDateTime.parse(isoDateTime);
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Format LocalDateTime
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Format LocalDate
     */
    public static String formatDate(java.time.LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format với pattern tuỳ ý
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
