import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  applyEach,
  applyWhenValue,
  FormField,
  form,
  maxLength,
  min,
  minLength,
  required,
} from '@angular/forms/signals';

import { Recipe, RecipeIngredient, RecipeRequest } from '../../entities/recipe-entity';
import { RecipeService } from '../../services/recipe';

interface IngredientInput {
  name: string;
  category: string;
  amount: number | '';
  unit: string;
  note: string;
}

interface RecipeFormModel {
  title: string;
  description: string;
  instructions: string;
  cookingTimeMinutes: number | '';
  category: string;
  difficulty: string;
  ingredients: IngredientInput[];
}

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [FormField],
  templateUrl: './recipe-form.html',
  styleUrl: './recipe-form.scss',
})
export class RecipeForm implements OnInit {
  recipeService = inject(RecipeService);
  router = inject(Router);
  activatedRoute = inject(ActivatedRoute);

  pageTitle = signal('');
  recipeId = signal<number | undefined>(undefined);
  submitted = signal(false);
  loading = signal(false);
  saving = signal(false);
  errorMessage = signal('');

  model = signal<RecipeFormModel>({
    title: '',
    description: '',
    instructions: '',
    cookingTimeMinutes: '',
    category: '',
    difficulty: '',
    ingredients: [this.emptyIngredient()],
  });

  recipeForm = form(this.model, schema => {
    required(schema.title, { message: 'Title is required' });
    minLength(schema.title, 2, { message: 'Title must have at least 2 characters' });
    maxLength(schema.title, 200, { message: 'Title is too long' });

    maxLength(schema.description, 500, { message: 'Description is too long' });

    required(schema.instructions, { message: 'Instructions are required' });
    minLength(schema.instructions, 5, { message: 'Instructions must have at least 5 characters' });


    min(schema.cookingTimeMinutes, 1, { message: 'Cooking time must be positive' });

    required(schema.category, { message: 'Category is required' });
    required(schema.difficulty, { message: 'Difficulty is required' });

    applyEach(schema.ingredients, ingredient => {
      required(ingredient.name, { message: 'Ingredient name is required' });
      minLength(ingredient.name, 2, { message: 'Ingredient name must have at least 2 characters' });

      required(ingredient.category, { message: 'Ingredient category is required' });


      min(ingredient.amount, 0, { message: 'Amount cant be negative' });

      maxLength(ingredient.unit, 30, { message: 'Unit is too long' });
      maxLength(ingredient.note, 200, { message: 'Note is too long' });
    });
  });

  ngOnInit(): void {
    const id = Number(this.activatedRoute.snapshot.params['id']);

    if (!Number.isNaN(id)) {
      this.pageTitle.set('Edit recipe');
      this.recipeId.set(id);
      this.loadRecipe(id);
    } else {
      this.pageTitle.set('Add new recipe');
    }
  }

  private loadRecipe(id: number): void {
    this.loading.set(true);

    this.recipeService.getRecipe(id).subscribe({
      next: (recipe: Recipe) => {
        this.model.set({
          title: recipe.title ?? '',
          description: recipe.description ?? '',
          instructions: recipe.instructions ?? '',
          cookingTimeMinutes: recipe.cookingTimeMinutes ?? '',
          category: recipe.category ?? '',
          difficulty: recipe.difficulty ?? '',
          ingredients:
            recipe.ingredients && recipe.ingredients.length > 0
              ? recipe.ingredients.map(ingredient => this.mapIngredientToInput(ingredient))
              : [this.emptyIngredient()],
        });

        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load recipe.');
        this.loading.set(false);
      },
    });
  }

  private mapIngredientToInput(ingredient: RecipeIngredient): IngredientInput {
    return {
      name: ingredient.name ?? '',
      category: ingredient.category ?? '',
      amount: ingredient.amount ?? '',
      unit: ingredient.unit ?? '',
      note: ingredient.note ?? '',
    };
  }

  emptyIngredient(): IngredientInput {
    return {
      name: '',
      category: '',
      amount: '',
      unit: '',
      note: '',
    };
  }

  addIngredient(): void {
    this.model.update(model => ({
      ...model,
      ingredients: [...model.ingredients, this.emptyIngredient()],
    }));
  }

  removeIngredient(index: number): void {
    this.model.update(model => {
      const nextIngredients = model.ingredients.filter((_, i) => i !== index);

      return {
        ...model,
        ingredients: nextIngredients.length > 0 ? nextIngredients : [this.emptyIngredient()],
      };
    });
  }

  onSubmit(event?: Event): void {
    event?.preventDefault();
    this.submitted.set(true);
    this.errorMessage.set('');

    if (this.recipeForm().invalid()) {
      return;
    }

    const data = this.model();

    const request: RecipeRequest = {
      title: data.title.trim(),
      description: data.description.trim(),
      instructions: data.instructions.trim(),
      cookingTimeMinutes:
        data.cookingTimeMinutes === '' ? null : Number(data.cookingTimeMinutes),
      category: data.category,
      difficulty: data.difficulty,
      ingredients: data.ingredients.map(ingredient => ({
        name: ingredient.name.trim(),
        category: ingredient.category,
        amount: ingredient.amount === '' ? null : Number(ingredient.amount),
        unit: ingredient.unit.trim(),
        note: ingredient.note.trim(),
      })),
    };

    this.saving.set(true);

    this.recipeService.saveRecipe(request, this.recipeId()).subscribe({
      next: () => {
        this.router.navigateByUrl('/recipes');
      },
      error: () => {
        this.errorMessage.set('Failed to save recipe.');
        this.saving.set(false);
      },
    });
  }

  cancel(): void {
    this.router.navigateByUrl('/recipes');
  }
}
