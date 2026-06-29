import { Routes } from '@angular/router';
import { RecipesList } from './pages/recipes-list/recipes-list';
import { RecipeForm } from './pages/recipe-form/recipe-form';
import { Recommendations } from './pages/recommendations/recommendations';

export const routes: Routes = [
  { path: '', redirectTo: 'recipes', pathMatch: 'full' },

  { path: 'recipes', component: RecipesList },
  { path: 'recipes/new', component: RecipeForm },
  { path: 'recipes/edit/:id', component: RecipeForm },
  { path: 'recommendations', component: Recommendations },

  { path: '**', redirectTo: 'recipes' }
];
