package com.example.recipematcher.service;

import com.example.recipematcher.dto.RecommendationRequest;
import com.example.recipematcher.dto.RecommendationResponse;
import com.example.recipematcher.model.Recipe;
import com.example.recipematcher.model.RecipeIngredient;
import com.example.recipematcher.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {
    private final RecipeRepository recipeRepository;

    public RecommendationService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<RecommendationResponse> recommendRecipes(RecommendationRequest request) {
        if (request == null || request.ingredients() == null || request.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Ingredient list cannot be empty");
        }
        Set<String> userIngredients = mapToSet(request);
        List<Recipe> recipes = recipeRepository.findAll();
        List<RecommendationResponse> recommendationResponses = new ArrayList<>();
        for (Recipe recipe : recipes) {
            recommendationResponses.add(calculateRecommendation(recipe, userIngredients));
        }
        recommendationResponses.sort(
                Comparator.comparingDouble(RecommendationResponse::matchPercentage).reversed()
        );
        return recommendationResponses;
    }

    private Set<String> mapToSet(RecommendationRequest request) {
        Set<String> set = new HashSet<>();
        for (String ingredient : request.ingredients()) {
            set.add(normalizeIngredientName(ingredient));
        }
        return set;
    }

    private String normalizeIngredientName(String name) {
        return name.trim().toLowerCase();
    }

    private RecommendationResponse calculateRecommendation(Recipe recipe, Set<String> userIngredients) {
        Set<String> recipeIngredientNames = new HashSet<>();
        for (RecipeIngredient recipeIngredient : recipe.getRecipeIngredients()) {
            String ingredientName = recipeIngredient.getIngredient().getName();
            recipeIngredientNames.add(normalizeIngredientName(ingredientName));
        }
        Set<String> matchedIngredients = new HashSet<>(recipeIngredientNames);
        matchedIngredients.retainAll(userIngredients);

        Set<String> missingIngredients = new HashSet<>(recipeIngredientNames);
        missingIngredients.removeAll(userIngredients);

        double percentage = 0.0;
        if (!recipeIngredientNames.isEmpty()) {
            percentage = (matchedIngredients.size() / (double) recipeIngredientNames.size()) * 100;
        }
        return new RecommendationResponse(
                recipe.getId(),
                recipe.getTitle(),
                percentage,
                new ArrayList<>(matchedIngredients),
                new ArrayList<>(missingIngredients)
        );
    }

    public List<RecommendationResponse> recommendRecipesWithMaxMissingIngredients(RecommendationRequest request, int maxMissing) {
        if (maxMissing < 0) {
            throw new IllegalArgumentException("Max missing ingredients cannot be negative");
        }
        List<RecommendationResponse> recommendations = recommendRecipes(request);
        return recommendations.stream()
                .filter(recommendation -> recommendation.missingIngredients().size() <= maxMissing)
                .toList();
    }
}
