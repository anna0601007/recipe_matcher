package com.example.recipematcher.service;

import com.example.recipematcher.dto.RecipeIngredientRequest;
import com.example.recipematcher.dto.RecipeIngredientResponse;
import com.example.recipematcher.dto.RecipeRequest;
import com.example.recipematcher.dto.RecipeResponse;
import com.example.recipematcher.enums.RecipeCategory;
import com.example.recipematcher.enums.RecipeDifficulty;
import com.example.recipematcher.model.Ingredient;
import com.example.recipematcher.model.Recipe;
import com.example.recipematcher.model.RecipeIngredient;
import com.example.recipematcher.repository.IngredientRepository;
import com.example.recipematcher.repository.RecipeIngredientRepository;
import com.example.recipematcher.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         IngredientRepository ingredientRepository,
                         RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    private void validateRecipeRequest(RecipeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe request cannot be null"
            );
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe title cannot be empty"
            );
        }
        if (request.instructions() == null || request.instructions().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe instructions cannot be empty"
            );
        }
        if (request.cookingTimeMinutes() != null && request.cookingTimeMinutes() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cooking time must be positive"
            );
        }
        if (request.ingredients() == null || request.ingredients().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe must contain at least one ingredient"
            );
        }
    }

    public RecipeResponse createRecipe(RecipeRequest request) {
        validateRecipeRequest(request);
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

    public RecipeResponse getRecipeById(Long id) {
        Recipe recipe = findRecipeById(id);
        return mapToRecipeResponse(recipe);
    }

    private Recipe findRecipeById(Long id) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe id cannot be null"
            );
        }
        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);
        if (optionalRecipe.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Recipe not found with id: " + id
            );
        }
        return optionalRecipe.get();
    }

    public void deleteRecipe(Long id) {
        Recipe recipe = findRecipeById(id);
        recipeRepository.delete(recipe);
    }

    public RecipeResponse updateRecipe(Long id, RecipeRequest request) {
        validateRecipeRequest(request);
        Recipe recipe = findRecipeById(id);
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setInstructions(request.instructions());
        recipe.setCookingTimeMinutes(request.cookingTimeMinutes());
        recipe.setCategory(request.category());
        recipe.setDifficulty(request.difficulty());

        recipe.getRecipeIngredients().clear();
        recipeIngredientRepository.deleteByRecipeId(id);
        recipeRepository.flush();
        for (RecipeIngredientRequest ingredientRequest : request.ingredients()) {
            RecipeIngredient recipeIngredient = createRecipeIngredient(recipe, ingredientRequest);
            recipe.getRecipeIngredients().add(recipeIngredient);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToRecipeResponse(savedRecipe);
    }

    public List<RecipeResponse> getRecipesByCategory(RecipeCategory category) {
        return recipeRepository.findByCategory(category)
                .stream()
                .map(this::mapToRecipeResponse)
                .toList();
    }

    public List<RecipeResponse> getRecipesByDifficulty(RecipeDifficulty difficulty) {
        return recipeRepository.findByDifficulty(difficulty)
                .stream()
                .map(this::mapToRecipeResponse)
                .toList();
    }

    public List<RecipeResponse> getRecipesByMaxCookingTime(Integer maxMinutes) {
        return recipeRepository.findByCookingTimeMinutesLessThanEqual(maxMinutes)
                .stream()
                .map(this::mapToRecipeResponse)
                .toList();
    }
}
