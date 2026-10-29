package com.f1rsters.os_service.util;

public class SensitiveDataMasker {

    public static String maskCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.length() < 4) {
            return "***";
        }
        String cleaned = cpfCnpj.replaceAll("[^0-9]", "");
        if (cleaned.length() <= 4) {
            return "***";
        }
        int visible = 3;
        int masked = cleaned.length() - visible * 2;
        return cleaned.substring(0, visible) + "*".repeat(masked) + cleaned.substring(cleaned.length() - visible);
    }

    public static String maskPlaca(String placa) {
        if (placa == null || placa.length() < 3) {
            return "***";
        }
        String cleaned = placa.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (cleaned.length() <= 3) {
            return "***";
        }
        return cleaned.substring(0, 2) + "**" + cleaned.substring(cleaned.length() - 1);
    }
}
