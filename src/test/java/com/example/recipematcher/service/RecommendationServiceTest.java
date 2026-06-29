package com.example.recipematcher.service;

import com.example.recipematcher.dto.RecommendationRequest;
import com.example.recipematcher.dto.RecommendationResponse;
import com.example.recipematcher.enums.IngredientCategory;
import com.example.recipematcher.model.Ingredient;
import com.example.recipematcher.model.Recipe;
import com.example.recipematcher.model.RecipeIngredient;
import com.example.recipematcher.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(recipeRepository);
    }

    @Test
    void recommendRecipesShouldReturnRecommendationsSortedByMatchPercentage() {
        Recipe omelette = recipe(1L, "Omelette", List.of("egg", "cheese", "butter"));
        Recipe pancakes = recipe(2L, "Pancakes", List.of("flour", "milk", "egg"));
        Recipe salad = recipe(3L, "Salad", List.of("tomato", "cucumber", "oil"));

        when(recipeRepository.findAll()).thenReturn(List.of(salad, pancakes, omelette));

        RecommendationRequest request = new RecommendationRequest(List.of("egg", "cheese"));

        List<RecommendationResponse> responses = recommendationService.recommendRecipes(request);

        assertEquals(3, responses.size());
        assertEquals("Omelette", responses.get(0).title());
        assertEquals("Pancakes", responses.get(1).title());
        assertEquals("Salad", responses.get(2).title());

        assertEquals(66.66666666666666, responses.get(0).matchPercentage());
        assertEquals(33.33333333333333, responses.get(1).matchPercentage());
        assertEquals(0.0, responses.get(2).matchPercentage());
    }

    @Test
    void recommendRecipesShouldCalculateMatchedAndMissingIngredients() {
        Recipe recipe = recipe(1L, "Omelette", List.of("egg", "cheese", "butter"));

        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        RecommendationRequest request = new RecommendationRequest(List.of("egg", "cheese"));

        List<RecommendationResponse> responses = recommendationService.recommendRecipes(request);

        RecommendationResponse response = responses.get(0);

        assertEquals(66.66666666666666, response.matchPercentage());
        assertEquals(2, response.matchedIngredients().size());
        assertTrue(response.matchedIngredients().contains("egg"));
        assertTrue(response.matchedIngredients().contains("cheese"));

        assertEquals(1, response.missingIngredients().size());
        assertTrue(response.missingIngredients().contains("butter"));
    }

    @Test
    void recommendRecipesShouldMatchIngredientsCaseInsensitiveAndTrimSpaces() {
        Recipe recipe = recipe(1L, "Omelette", List.of("egg", "cheese"));

        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        RecommendationRequest request = new RecommendationRequest(List.of(" EGG ", "CheEse"));

        List<RecommendationResponse> responses = recommendationService.recommendRecipes(request);

        assertEquals(100.0, responses.get(0).matchPercentage());
        assertEquals(2, responses.get(0).matchedIngredients().size());
        assertEquals(0, responses.get(0).missingIngredients().size());
    }

    @Test
    void recommendRecipesShouldReturnZeroPercentageForRecipeWithoutIngredients() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Empty recipe");

        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        RecommendationRequest request = new RecommendationRequest(List.of("egg"));

        List<RecommendationResponse> responses = recommendationService.recommendRecipes(request);

        assertEquals(0.0, responses.get(0).matchPercentage());
        assertTrue(responses.get(0).matchedIngredients().isEmpty());
        assertTrue(responses.get(0).missingIngredients().isEmpty());
    }

    @Test
    void recommendRecipesShouldThrowExceptionWhenRequestIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recommendationService.recommendRecipes(null)
        );

        assertEquals("Ingredient list cannot be empty", exception.getMessage());
    }

    @Test
    void recommendRecipesShouldThrowExceptionWhenIngredientListIsNull() {
        RecommendationRequest request = new RecommendationRequest(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recommendationService.recommendRecipes(request)
        );

        assertEquals("Ingredient list cannot be empty", exception.getMessage());
    }

    @Test
    void recommendRecipesShouldThrowExceptionWhenIngredientListIsEmpty() {
        RecommendationRequest request = new RecommendationRequest(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recommendationService.recommendRecipes(request)
        );

        assertEquals("Ingredient list cannot be empty", exception.getMessage());
    }

    @Test
    void recommendRecipesWithMaxMissingIngredientsShouldFilterRecommendations() {
        Recipe omelette = recipe(1L, "Omelette", List.of("egg", "cheese", "butter"));
        Recipe pancakes = recipe(2L, "Pancakes", List.of("flour", "milk", "egg"));
        Recipe salad = recipe(3L, "Salad", List.of("tomato", "cucumber", "oil"));

        when(recipeRepository.findAll()).thenReturn(List.of(omelette, pancakes, salad));

        RecommendationRequest request = new RecommendationRequest(List.of("egg", "cheese"));

        List<RecommendationResponse> responses =
                recommendationService.recommendRecipesWithMaxMissingIngredients(request, 1);

        assertEquals(1, responses.size());
        assertEquals("Omelette", responses.get(0).title());
        assertEquals(1, responses.get(0).missingIngredients().size());
    }

    @Test
    void recommendRecipesWithMaxMissingIngredientsShouldAllowExactZeroMissing() {
        Recipe omelette = recipe(1L, "Omelette", List.of("egg", "cheese"));

        when(recipeRepository.findAll()).thenReturn(List.of(omelette));

        RecommendationRequest request = new RecommendationRequest(List.of("egg", "cheese"));

        List<RecommendationResponse> responses =
                recommendationService.recommendRecipesWithMaxMissingIngredients(request, 0);

        assertEquals(1, responses.size());
        assertEquals("Omelette", responses.get(0).title());
    }

    @Test
    void recommendRecipesWithMaxMissingIngredientsShouldThrowExceptionWhenMaxMissingIsNegative() {
        RecommendationRequest request = new RecommendationRequest(List.of("egg"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recommendationService.recommendRecipesWithMaxMissingIngredients(request, -1)
        );

        assertEquals("Max missing ingredients cannot be negative", exception.getMessage());
    }

    private Recipe recipe(Long id, String title, List<String> ingredientNames) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setTitle(title);

        for (String ingredientName : ingredientNames) {
            Ingredient ingredient = new Ingredient();
            ingredient.setName(ingredientName);
            ingredient.setCategory(IngredientCategory.OTHER);

            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setRecipe(recipe);
            recipeIngredient.setIngredient(ingredient);

            recipe.getRecipeIngredients().add(recipeIngredient);
        }

        return recipe;
    }
}