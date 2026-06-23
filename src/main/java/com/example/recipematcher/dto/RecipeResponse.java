package com.example.recipematcher.dto;

import com.example.recipematcher.enums.RecipeCategory;
import com.example.recipematcher.enums.RecipeDifficulty;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeResponse(
        Long id,
        String title,
        String description,
        String instructions,
        Integer cookingTimeMinutes,
        RecipeCategory category,
        RecipeDifficulty difficulty,
        LocalDateTime createdAt,
        List<RecipeIngredientResponse> ingredients
) {
}