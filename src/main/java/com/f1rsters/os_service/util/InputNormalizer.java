package com.f1rsters.os_service.util;

public class InputNormalizer {

    public static String normalizeCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null) {
            return null;
        }
        return cpfCnpj.replaceAll("[^0-9]", "");
    }

    public static String normalizePlaca(String placa) {
        if (placa == null) {
            return null;
        }
        return placa.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
}
