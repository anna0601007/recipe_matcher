import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { catchError, EMPTY, map, Observable } from 'rxjs';
import { Recipe, RecipeRequest } from '../entities/recipe-entity';
import { MessageService } from './message';

@Injectable({
  providedIn: 'root',
})
export class RecipeService {
  http = inject(HttpClient);
  messageService = inject(MessageService);
  url = 'http://localhost:8080/api/';

  getRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(this.url + 'recipes').pipe(
      catchError(error => this.processError(error))
    );
  }

  getRecipe(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(this.url + 'recipes/' + id).pipe(
      catchError(error => this.processError(error))
    );
  }

  saveRecipe(request: RecipeRequest, id?: number): Observable<Recipe> {
    if (id !== undefined) {
      return this.http.put<Recipe>(this.url + 'recipes/' + id, request).pipe(
        catchError(error => this.processError(error))
      );
    }

    return this.http.post<Recipe>(this.url + 'recipes', request).pipe(
      catchError(error => this.processError(error))
    );
  }

  deleteRecipe(id: number): Observable<boolean> {
    return this.http.delete<void>(this.url + 'recipes/' + id).pipe(
      map(() => true),
      catchError(error => this.processError(error))
    );
  }

  filterRecipes(category?: string, difficulty?: string, maxCookingTime?: number): Observable<Recipe[]> {
    let params = new HttpParams();
    if (category) {
      params = params.set('category', category);
    }
    if (difficulty) {
      params = params.set('difficulty', difficulty);
    }
    if (maxCookingTime !== undefined || maxCookingTime) {
      params = params.set('maxCookingTime', maxCookingTime);
    }
    return this.http.get<Recipe[]>(this.url + 'recipes/filter', { params }).pipe(
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
        this.messageService.errorMessage(
          'Server has some serious problems, contact administrator.'
        );
        return EMPTY;
      }
    }
    console.error('HTTP connection error', error);
    return EMPTY;
  }
}
