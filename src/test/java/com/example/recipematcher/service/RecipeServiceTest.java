package com.example.recipematcher.service;

import com.example.recipematcher.dto.RecipeIngredientRequest;
import com.example.recipematcher.dto.RecipeRequest;
import com.example.recipematcher.dto.RecipeResponse;
import com.example.recipematcher.enums.IngredientCategory;
import com.example.recipematcher.enums.MeasurementUnit;
import com.example.recipematcher.enums.RecipeCategory;
import com.example.recipematcher.enums.RecipeDifficulty;
import com.example.recipematcher.model.Ingredient;
import com.example.recipematcher.model.Recipe;
import com.example.recipematcher.model.RecipeIngredient;
import com.example.recipematcher.repository.IngredientRepository;
import com.example.recipematcher.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, ingredientRepository);
    }

    @Test
    void createRecipeShouldSaveRecipeWithNormalizedIngredient() {
        RecipeRequest request = validRequest();

        when(ingredientRepository.findByName("egg")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe recipe = invocation.getArgument(0);
            recipe.setId(1L);
            return recipe;
        });

        RecipeResponse response = recipeService.createRecipe(request);

        assertEquals(1L, response.id());
        assertEquals("Omelette", response.title());
        assertEquals(1, response.ingredients().size());
        assertEquals("egg", response.ingredients().getFirst().name());

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(recipeCaptor.capture());

        Recipe savedRecipe = recipeCaptor.getValue();
        assertEquals("Omelette", savedRecipe.getTitle());
        assertEquals(1, savedRecipe.getRecipeIngredients().size());
        assertSame(savedRecipe, savedRecipe.getRecipeIngredients().getFirst().getRecipe());
    }

    @Test
    void createRecipeShouldReuseExistingIngredient() {
        Ingredient existingIngredient = ingredient("egg", IngredientCategory.DAIRY);

        when(ingredientRepository.findByName("egg")).thenReturn(Optional.of(existingIngredient));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recipeService.createRecipe(validRequest());

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    @Test
    void getAllRecipesShouldReturnMappedResponses() {
        Recipe recipe = recipe(1L, "Pancakes", RecipeCategory.BREAKFAST, RecipeDifficulty.EASY, 20);

        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        List<RecipeResponse> responses = recipeService.getAllRecipes();

        assertEquals(1, responses.size());
        assertEquals("Pancakes", responses.getFirst().title());
    }

    @Test
    void getRecipeByIdShouldReturnRecipeWhenFound() {
        Recipe recipe = recipe(1L, "Pancakes", RecipeCategory.BREAKFAST, RecipeDifficulty.EASY, 20);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        RecipeResponse response = recipeService.getRecipeById(1L);

        assertEquals(1L, response.id());
        assertEquals("Pancakes", response.title());
    }

    @Test
    void getRecipeByIdShouldThrowNotFoundWhenRecipeDoesNotExist() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.getRecipeById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Recipe not found with id: 99", exception.getReason());
    }

    @Test
    void deleteRecipeShouldDeleteExistingRecipe() {
        Recipe recipe = recipe(1L, "Pancakes", RecipeCategory.BREAKFAST, RecipeDifficulty.EASY, 20);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(1L);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void updateRecipeShouldReplaceRecipeDataAndIngredients() {
        Recipe existingRecipe = recipe(1L, "Old title", RecipeCategory.BREAKFAST, RecipeDifficulty.EASY, 20);
        RecipeRequest request = new RecipeRequest(
                "Updated title",
                "Updated description",
                "Updated instructions",
                30,
                RecipeCategory.DINNER,
                RecipeDifficulty.MEDIUM,
                List.of(ingredientRequest("Cheese"))
        );

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existingRecipe));
        when(ingredientRepository.findByName("cheese")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeResponse response = recipeService.updateRecipe(1L, request);

        assertEquals("Updated title", response.title());
        assertEquals(RecipeCategory.DINNER, response.category());
        assertEquals(RecipeDifficulty.MEDIUM, response.difficulty());
        assertEquals(30, response.cookingTimeMinutes());
        assertEquals(1, response.ingredients().size());
        assertEquals("cheese", response.ingredients().getFirst().name());

        verify(recipeRepository).flush();
        verify(recipeRepository).save(existingRecipe);
    }

    @Test
    void getRecipesByCategoryShouldUseRepository() {
        Recipe recipe = recipe(1L, "Soup", RecipeCategory.SOUP, RecipeDifficulty.EASY, 35);

        when(recipeRepository.findByCategory(RecipeCategory.SOUP)).thenReturn(List.of(recipe));

        List<RecipeResponse> responses = recipeService.getRecipesByCategory(RecipeCategory.SOUP);

        assertEquals(1, responses.size());
        assertEquals(RecipeCategory.SOUP, responses.getFirst().category());
    }

    @Test
    void getRecipesByDifficultyShouldUseRepository() {
        Recipe recipe = recipe(1L, "Soup", RecipeCategory.SOUP, RecipeDifficulty.HARD, 35);

        when(recipeRepository.findByDifficulty(RecipeDifficulty.HARD)).thenReturn(List.of(recipe));

        List<RecipeResponse> responses = recipeService.getRecipesByDifficulty(RecipeDifficulty.HARD);

        assertEquals(1, responses.size());
        assertEquals(RecipeDifficulty.HARD, responses.getFirst().difficulty());
    }

    @Test
    void getRecipesByMaxCookingTimeShouldUseRepository() {
        Recipe recipe = recipe(1L, "Omelette", RecipeCategory.BREAKFAST, RecipeDifficulty.EASY, 11);

        when(recipeRepository.findByCookingTimeMinutesLessThanEqual(15)).thenReturn(List.of(recipe));

        List<RecipeResponse> responses = recipeService.getRecipesByMaxCookingTime(15);

        assertEquals(1, responses.size());
        assertEquals(11, responses.getFirst().cookingTimeMinutes());
    }

    @Test
    void filterRecipesShouldFilterByCategoryDifficultyAndMaxCookingTime() {
        Recipe matchingRecipe = recipe(1L, "Omelette", RecipeCategory.BREAKFAST, RecipeDifficulty.EASY, 11);
        Recipe wrongCategory = recipe(2L, "Salad", RecipeCategory.SALAD, RecipeDifficulty.EASY, 10);
        Recipe wrongDifficulty = recipe(3L, "Pancakes", RecipeCategory.BREAKFAST, RecipeDifficulty.HARD, 15);
        Recipe tooLong = recipe(4L, "Soup", RecipeCategory.BREAKFAST, RecipeDifficulty.EASY, 60);

        when(recipeRepository.findAll()).thenReturn(List.of(
                matchingRecipe,
                wrongCategory,
                wrongDifficulty,
                tooLong
        ));

        List<RecipeResponse> responses = recipeService.filterRecipes(
                RecipeCategory.BREAKFAST,
                RecipeDifficulty.EASY,
                20
        );

        assertEquals(1, responses.size());
        assertEquals("Omelette", responses.getFirst().title());
    }

    @Test
    void createRecipeShouldThrowBadRequestForEmptyTitle() {
        RecipeRequest request = new RecipeRequest(
                "",
                "Description",
                "Instructions",
                20,
                RecipeCategory.BREAKFAST,
                RecipeDifficulty.EASY,
                List.of(ingredientRequest("Egg"))
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.createRecipe(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Recipe title cannot be empty", exception.getReason());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipeShouldThrowBadRequestForEmptyInstructions() {
        RecipeRequest request = new RecipeRequest(
                "Omelette",
                "Description",
                "",
                20,
                RecipeCategory.BREAKFAST,
                RecipeDifficulty.EASY,
                List.of(ingredientRequest("Egg"))
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.createRecipe(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Recipe instructions cannot be empty", exception.getReason());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipeShouldThrowBadRequestForNegativeCookingTime() {
        RecipeRequest request = new RecipeRequest(
                "Omelette",
                "Description",
                "Instructions",
                -1,
                RecipeCategory.BREAKFAST,
                RecipeDifficulty.EASY,
                List.of(ingredientRequest("Egg"))
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.createRecipe(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Cooking time must be positive", exception.getReason());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipeShouldThrowBadRequestForEmptyIngredients() {
        RecipeRequest request = new RecipeRequest(
                "Omelette",
                "Description",
                "Instructions",
                20,
                RecipeCategory.BREAKFAST,
                RecipeDifficulty.EASY,
                List.of()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.createRecipe(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Recipe must contain at least one ingredient", exception.getReason());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipeShouldThrowBadRequestForDuplicateIngredients() {
        RecipeRequest request = new RecipeRequest(
                "Omelette",
                "Description",
                "Instructions",
                20,
                RecipeCategory.BREAKFAST,
                RecipeDifficulty.EASY,
                List.of(
                        ingredientRequest("Egg"),
                        ingredientRequest(" egg ")
                )
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.createRecipe(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Recipe cannot contain duplicate ingredients: egg", exception.getReason());
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void getRecipesByMaxCookingTimeShouldThrowBadRequestForInvalidValue() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.getRecipesByMaxCookingTime(0)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Maximum cooking time must be positive", exception.getReason());
    }

    @Test
    void filterRecipesShouldThrowBadRequestForInvalidMaxCookingTime() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.filterRecipes(null, null, 0)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Maximum cooking time must be positive", exception.getReason());
    }

    private RecipeRequest validRequest() {
        return new RecipeRequest(
                "Omelette",
                "Easy egg omelette",
                "Beat eggs and fry with butter",
                11,
                RecipeCategory.BREAKFAST,
                RecipeDifficulty.EASY,
                List.of(ingredientRequest("Egg"))
        );
    }

    private RecipeIngredientRequest ingredientRequest(String name) {
        return new RecipeIngredientRequest(
                name,
                IngredientCategory.DAIRY,
                BigDecimal.valueOf(2),
                MeasurementUnit.PIECE,
                null
        );
    }

    private Ingredient ingredient(String name, IngredientCategory category) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName(name);
        ingredient.setCategory(category);
        return ingredient;
    }

    private Recipe recipe(
            Long id,
            String title,
            RecipeCategory category,
            RecipeDifficulty difficulty,
            Integer cookingTimeMinutes
    ) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setTitle(title);
        recipe.setDescription(title + " description");
        recipe.setInstructions(title + " instructions");
        recipe.setCategory(category);
        recipe.setDifficulty(difficulty);
        recipe.setCookingTimeMinutes(cookingTimeMinutes);

        Ingredient egg = ingredient("egg", IngredientCategory.DAIRY);

        RecipeIngredient recipeIngredient = new RecipeIngredient();
        recipeIngredient.setRecipe(recipe);
        recipeIngredient.setIngredient(egg);
        recipeIngredient.setAmount(BigDecimal.valueOf(2));
        recipeIngredient.setUnit(MeasurementUnit.PIECE);
        recipeIngredient.setNote(null);

        recipe.getRecipeIngredients().add(recipeIngredient);

        return recipe;
    }
}