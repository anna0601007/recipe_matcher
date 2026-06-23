package com.example.recipematcher.repository;

import com.example.recipematcher.enums.RecipeCategory;
import com.example.recipematcher.enums.RecipeDifficulty;
import com.example.recipematcher.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByCategory(RecipeCategory category);

    List<Recipe> findByDifficulty(RecipeDifficulty difficulty);

    List<Recipe> findByCookingTimeMinutesLessThanEqual(Integer maxMinutes);
}