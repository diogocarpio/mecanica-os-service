package com.f1rsters.os_service.validation;

import com.f1rsters.os_service.util.CpfCnpjValidator;

public class CpfCnpjConstraintValidator implements jakarta.validation.ConstraintValidator<CpfCnpjValido, String> {

    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return CpfCnpjValidator.isValid(value);
    }
}
