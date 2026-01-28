package com.pms.propertymanagement.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {

    private static final Locale VIETNAM = new Locale("vi", "VN");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(VIETNAM);

    public static String formatVND(Double amount) {
        if (amount == null) {
            return "0 đ";
        }
        return CURRENCY_FORMATTER.format(amount);
    }
}
