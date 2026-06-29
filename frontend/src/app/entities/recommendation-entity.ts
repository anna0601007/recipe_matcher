export interface RecommendationRequest {
  ingredients: string[];
}

export class Recommendation {
  static clone(recommendation: Recommendation): Recommendation {
    return new Recommendation(
      recommendation.recipeId,
      recommendation.title,
      recommendation.matchPercentage,
      recommendation.matchedIngredients || [],
      recommendation.missingIngredients || []
    );
  }

  constructor(
    public recipeId: number,
    public title: string,
    public matchPercentage: number,
    public matchedIngredients: string[],
    public missingIngredients: string[]
  ) {}
}
