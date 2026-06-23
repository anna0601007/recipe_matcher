package com.example.recipematcher.repository;

import com.example.recipematcher.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    void deleteByRecipeId(Long recipeId);
}