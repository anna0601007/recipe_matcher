package com.example.recipematcher.controller;

import com.example.recipematcher.dto.RecipeRequest;
import com.example.recipematcher.dto.RecipeResponse;
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
}