package com.example.recipematcher.controller;

import com.example.recipematcher.dto.RecipeRequest;
import com.example.recipematcher.dto.RecipeResponse;
import com.example.recipematcher.enums.RecipeCategory;
import com.example.recipematcher.enums.RecipeDifficulty;
import com.example.recipematcher.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public RecipeResponse createRecipe(@RequestBody RecipeRequest request) {
        return recipeService.createRecipe(request);
    }

    @GetMapping
    public List<RecipeResponse> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public RecipeResponse getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
    }

    @PutMapping("/{id}")
    public RecipeResponse updateRecipe(@PathVariable Long id, @RequestBody RecipeRequest request) {
        return recipeService.updateRecipe(id, request);
    }

    @GetMapping("/category/{category}")
    public List<RecipeResponse> getRecipesByCategory(@PathVariable RecipeCategory category) {
        return recipeService.getRecipesByCategory(category);
    }

    @GetMapping("/difficulty/{difficulty}")
    public List<RecipeResponse> getRecipesByDifficulty(@PathVariable RecipeDifficulty difficulty) {
        return recipeService.getRecipesByDifficulty(difficulty);
    }

    @GetMapping("/max-time/{maxMinutes}")
    public List<RecipeResponse> getRecipesByMaxCookingTime(@PathVariable Integer maxMinutes) {
        return recipeService.getRecipesByMaxCookingTime(maxMinutes);
    }
}