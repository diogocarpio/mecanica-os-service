package com.f1rsters.os_service.util;

public class CpfCnpjValidator {

    public static boolean isValid(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.trim().isEmpty()) {
            return false;
        }

        String cleaned = cpfCnpj.replaceAll("[^0-9]", "");

        if (cleaned.length() == 11) {
            return isValidCPF(cleaned);
        } else if (cleaned.length() == 14) {
            return isValidCNPJ(cleaned);
        }

        return false;
    }

    private static boolean isValidCPF(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        int[] weights = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        int d1 = calculateDigit(cpf.substring(0, 9), weights);
        int d2 = calculateDigit(cpf.substring(0, 9) + d1, weights);

        return cpf.equals(cpf.substring(0, 9) + d1 + d2);
    }

    private static boolean isValidCNPJ(String cnpj) {
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int d1 = calculateDigit(cnpj.substring(0, 12), weights1);
        int d2 = calculateDigit(cnpj.substring(0, 12) + d1, weights2);

        return cnpj.equals(cnpj.substring(0, 12) + d1 + d2);
    }

    private static int calculateDigit(String str, int[] weights) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            sum += Character.getNumericValue(str.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
