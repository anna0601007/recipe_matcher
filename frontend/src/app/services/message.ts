import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  snackBar = inject(MatSnackBar);

  errorMessage(message: string, duration = 3000): void {
    this.snackBar.open(message, 'ERROR', {
      duration
    });
  }

  successMessage(message: string, duration = 3000): void {
    this.snackBar.open(message, 'SUCCESS', {
      duration
    });
  }
}
