package com.example.recipematcher.dto;

import java.util.List;

public record RecommendationResponse(
        Long recipeId,
        String title,
        Double matchPercentage,
        List<String> matchedIngredients,
        List<String> missingIngredients
) {
}