package com.example.recipematcher.dto;

import com.example.recipematcher.enums.RecipeCategory;
import com.example.recipematcher.enums.RecipeDifficulty;

import java.time.LocalDateTime;

public record RecipeRequest(
        Long id,
        String title,
        String description,
        String instructions,
        Integer cooking_time_minutes,
        RecipeCategory category,
        RecipeDifficulty difficulty,
        LocalDateTime created_at
) {
}
