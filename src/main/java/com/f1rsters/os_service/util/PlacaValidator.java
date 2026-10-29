package com.f1rsters.os_service.util;

import java.util.regex.Pattern;

public class PlacaValidator {

    private static final Pattern PLACA_MERCOSUL = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");
    private static final Pattern PLACA_ANTIGA = Pattern.compile("^[A-Z]{3}[0-9]{4}$");

    public static boolean isValid(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return false;
        }

        String cleaned = placa.toUpperCase().replaceAll("[^A-Z0-9]", "");

        return PLACA_MERCOSUL.matcher(cleaned).matches() || PLACA_ANTIGA.matcher(cleaned).matches();
    }
}
