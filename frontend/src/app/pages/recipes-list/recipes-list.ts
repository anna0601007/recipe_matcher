import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Recipe } from '../../entities/recipe-entity';
import { RecipeService } from '../../services/recipe';

@Component({
  selector: 'app-recipes-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './recipes-list.html',
  styleUrl: './recipes-list.scss'
})
export class RecipesList {
  recipeService = inject(RecipeService);
  recipes = signal<Recipe[]>([]);
  loading = signal(false);
  deletingRecipeId = signal<number | null>(null);
  selectedCategory = signal('');
  selectedDifficulty = signal('');
  maxCookingTime = signal('');
  recipesCount = computed(() => this.recipes().length);
  filtersActive = computed(() =>
    !!this.selectedCategory() ||
    !!this.selectedDifficulty() ||
    !!this.maxCookingTime()
  );

  categories = [
    { value: 'BREAKFAST', label: 'Breakfast' },
    { value: 'LUNCH', label: 'Lunch' },
    { value: 'DINNER', label: 'Dinner' },
    { value: 'DESSERT', label: 'Dessert' },
    { value: 'SNACK', label: 'Snack' },
    { value: 'DRINK', label: 'Drink' },
    { value: 'OTHER', label: 'Other' }
  ];

  difficulties = [
    { value: 'EASY', label: 'Easy' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HARD', label: 'Hard' }
  ];

  constructor() {
    this.loadRecipes();
  }

  loadRecipes(): void {
    this.loading.set(true);
    this.recipeService.getRecipes()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(recipes => {
        this.recipes.set(recipes);
      });
  }

  applyFilters(): void {
    const maxCookingTimeValue = this.maxCookingTime()
      ? Number(this.maxCookingTime())
      : undefined;
    if (maxCookingTimeValue !== undefined && maxCookingTimeValue < 1) {
      this.maxCookingTime.set('1');
      return;
    }
    this.loading.set(true);
    this.recipeService
      .filterRecipes(
        this.selectedCategory() || undefined,
        this.selectedDifficulty() || undefined,
        maxCookingTimeValue
      )
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(recipes => {
        this.recipes.set(recipes);
      });
  }

  clearFilters(): void {
    this.selectedCategory.set('');
    this.selectedDifficulty.set('');
    this.maxCookingTime.set('');
    this.loadRecipes();
  }

  onCategoryChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedCategory.set(select.value);
  }

  onDifficultyChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedDifficulty.set(select.value);
  }

  onMaxCookingTimeChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    if (input.value !== '' && value < 1) {
      input.value = '1';
      this.maxCookingTime.set('1');
      return;
    }
    this.maxCookingTime.set(input.value);
  }

  deleteRecipe(recipe: Recipe): void {
    const confirmed = confirm('Delete recipe ' + recipe.title + '?');
    if (!confirmed) {
      return;
    }
    this.deletingRecipeId.set(recipe.id);
    this.recipeService.deleteRecipe(recipe.id)
      .pipe(finalize(() => this.deletingRecipeId.set(null)))
      .subscribe(success => {
        if (success) {
          this.recipes.set(this.recipes().filter(item => item.id !== recipe.id));
        }
      });
  }

  formatEnum(value: string): string {
    return value
      .toLowerCase()
      .replaceAll('_', ' ')
      .replace(/\b\w/g, char => char.toUpperCase());
  }
}
