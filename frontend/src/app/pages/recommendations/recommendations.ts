import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Recommendation } from '../../entities/recommendation-entity';
import { RecommendationService } from '../../services/recommendation';
import { MessageService } from '../../services/message';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './recommendations.html',
  styleUrl: './recommendations.scss'
})
export class Recommendations {
  fb = inject(FormBuilder);
  recommendationService = inject(RecommendationService);
  messageService = inject(MessageService);

  recommendations = signal<Recommendation[]>([]);
  loading = signal(false);
  searched = signal(false);

  recommendationsCount = computed(() => this.recommendations().length);

  form = this.fb.group({
    ingredientsText: ['', [Validators.required]],
    useMaxMissing: [false],
    maxMissing: [2, [Validators.required, Validators.min(0)]]
  });

  searchRecommendations(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const ingredients = this.parseIngredients();

    if (ingredients.length === 0) {
      this.messageService.errorMessage('Ingredient list cannot be empty');
      return;
    }

    const request = {
      ingredients
    };

    this.loading.set(true);
    this.searched.set(true);

    if (this.isMaxMissingEnabled()) {
      const maxMissing = Number(this.form.value.maxMissing);

      this.recommendationService
        .recommendRecipesWithMaxMissing(request, maxMissing)
        .pipe(finalize(() => this.loading.set(false)))
        .subscribe(recommendations => {
          this.recommendations.set(recommendations);
        });

      return;
    }

    this.recommendationService
      .recommendRecipes(request)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(recommendations => {
        this.recommendations.set(recommendations);
      });
  }

  parseIngredients(): string[] {
    const value = this.form.value.ingredientsText || '';

    return value
      .split(/[\n,]+/)
      .map(ingredient => ingredient.trim())
      .filter(ingredient => ingredient.length > 0);
  }

  clearForm(): void {
    this.form.reset({
      ingredientsText: '',
      useMaxMissing: false,
      maxMissing: 2
    });

    this.recommendations.set([]);
    this.searched.set(false);
  }

  isMaxMissingEnabled(): boolean {
    return !!this.form.value.useMaxMissing;
  }

  isIngredientsInvalid(): boolean {
    const field = this.form.get('ingredientsText');
    return !!field && field.invalid && field.touched;
  }

  isMaxMissingInvalid(): boolean {
    const field = this.form.get('maxMissing');
    return !!field && field.invalid && field.touched;
  }

  formatPercentage(value: number): string {
    return value.toFixed(1);
  }
}
