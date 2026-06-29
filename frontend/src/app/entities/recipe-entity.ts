export interface RecipeIngredient {
  name: string;
  category: string;
  amount: number | null;
  unit: string;
  note: string;
}

export interface Recipe {
  id: number;
  title: string;
  description: string;
  instructions: string;
  cookingTimeMinutes: number | null;
  category: string;
  difficulty: string;
  createdAt: string;
  ingredients: RecipeIngredient[];
}

export interface RecipeIngredientRequest {
  name: string;
  category: string;
  amount: number | null;
  unit: string;
  note: string;
}

export interface RecipeRequest {
  title: string;
  description: string;
  instructions: string;
  cookingTimeMinutes: number | null;
  category: string;
  difficulty: string;
  ingredients: RecipeIngredientRequest[];
}
