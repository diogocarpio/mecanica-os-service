package com.f1rsters.os_service.validation;

import com.f1rsters.os_service.util.PlacaValidator;

public class PlacaConstraintValidator implements jakarta.validation.ConstraintValidator<PlacaValida, String> {

    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return PlacaValidator.isValid(value);
    }
}
