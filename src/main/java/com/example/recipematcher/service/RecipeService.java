package com.example.recipematcher.service;

import com.example.recipematcher.dto.RecipeIngredientRequest;
import com.example.recipematcher.dto.RecipeIngredientResponse;
import com.example.recipematcher.dto.RecipeRequest;
import com.example.recipematcher.dto.RecipeResponse;
import com.example.recipematcher.model.Ingredient;
import com.example.recipematcher.model.Recipe;
import com.example.recipematcher.model.RecipeIngredient;
import com.example.recipematcher.repository.IngredientRepository;
import com.example.recipematcher.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public RecipeResponse createRecipe(RecipeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Recipe request cannot be null");
        }
        Recipe recipe = new Recipe();
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setInstructions(request.instructions());
        recipe.setCookingTimeMinutes(request.cookingTimeMinutes());
        recipe.setCategory(request.category());
        recipe.setDifficulty(request.difficulty());

        for (RecipeIngredientRequest ingredientRequest : request.ingredients()) {
            RecipeIngredient recipeIngredient = createRecipeIngredient(recipe, ingredientRequest);
            recipe.getRecipeIngredients().add(recipeIngredient);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToRecipeResponse(savedRecipe);
    }

    private RecipeIngredient createRecipeIngredient(Recipe recipe, RecipeIngredientRequest ingredientRequest) {
        String normalizedName = normalizeIngredientName(ingredientRequest.name());
        Ingredient ingredient = ingredientRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    Ingredient newIngredient = new Ingredient();
                    newIngredient.setName(normalizedName);
                    newIngredient.setCategory(ingredientRequest.category());
                    return ingredientRepository.save(newIngredient);
                });
        RecipeIngredient recipeIngredient = new RecipeIngredient();
        recipeIngredient.setRecipe(recipe);
        recipeIngredient.setIngredient(ingredient);
        recipeIngredient.setAmount(ingredientRequest.amount());
        recipeIngredient.setUnit(ingredientRequest.unit());
        recipeIngredient.setNote(ingredientRequest.note());
        return recipeIngredient;
    }

    private String normalizeIngredientName(String name) {
        return name.trim().toLowerCase();
    }

    private RecipeResponse mapToRecipeResponse(Recipe recipe) {
        List<RecipeIngredientResponse> ingredients = recipe.getRecipeIngredients()
                .stream()
                .map(this::mapToRecipeIngredientResponse)
                .toList();
        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getInstructions(),
                recipe.getCookingTimeMinutes(),
                recipe.getCategory(),
                recipe.getDifficulty(),
                recipe.getCreatedAt(),
                ingredients
        );
    }

    private RecipeIngredientResponse mapToRecipeIngredientResponse(RecipeIngredient recipeIngredient) {
        Ingredient ingredient = recipeIngredient.getIngredient();
        return new RecipeIngredientResponse(
                ingredient.getName(),
                ingredient.getCategory(),
                recipeIngredient.getAmount(),
                recipeIngredient.getUnit(),
                recipeIngredient.getNote()
        );
    }

    public List<RecipeResponse> getAllRecipes() {
        return recipeRepository.findAll()
                .stream()
                .map(this::mapToRecipeResponse)
                .toList();
    }

}
