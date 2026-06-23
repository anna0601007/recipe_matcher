package com.example.recipematcher.dto;

import com.example.recipematcher.enums.IngredientCategory;
import com.example.recipematcher.enums.MeasurementUnit;

import java.math.BigDecimal;

public record RecipeIngredientResponse(
        String name,
        IngredientCategory category,
        BigDecimal amount,
        MeasurementUnit unit,
        String note
) {
}