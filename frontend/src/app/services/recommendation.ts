import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY, map, Observable } from 'rxjs';

import { MessageService } from './message';
import { Recommendation, RecommendationRequest } from '../entities/recommendation-entity';

@Injectable({
  providedIn: 'root',
})
export class RecommendationService {
  http = inject(HttpClient);
  messageService = inject(MessageService);
  url = 'http://localhost:8080/api/';

  recommendRecipes(request: RecommendationRequest): Observable<Recommendation[]> {
    return this.http.post<Recommendation[]>(this.url + 'recommendations', request).pipe(
      map(jsonRecommendations =>
        jsonRecommendations.map(recommendation => Recommendation.clone(recommendation))
      ),
      catchError(error => this.processError(error))
    );
  }

  recommendRecipesWithMaxMissing(
    request: RecommendationRequest,
    maxMissing: number
  ): Observable<Recommendation[]> {
    return this.http
      .post<Recommendation[]>(`${this.url}recommendations/max-missing/${maxMissing}`, request)
      .pipe(
        map(jsonRecommendations =>
          jsonRecommendations.map(recommendation => Recommendation.clone(recommendation))
        ),
        catchError(error => this.processError(error))
      );
  }

  processError(error: unknown): Observable<never> {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.messageService.errorMessage('Server not available');
        return EMPTY;
      }
      if (error.status >= 400 && error.status < 500) {
        const message =
          error.error?.message ||
          error.error?.errorMessage ||
          'Request contains invalid data';
        this.messageService.errorMessage(message);
        return EMPTY;
      }
      if (error.status >= 500) {
        this.messageService.errorMessage('Server has some serious problems, contact administrator.');
        return EMPTY;
      }
    }
    console.error('HTTP connection error', error);
    return EMPTY;
  }
}
