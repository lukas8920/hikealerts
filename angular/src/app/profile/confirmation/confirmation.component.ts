import { Component } from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrl: './confirmation.component.css'
})
export class ConfirmationComponent {
  constructor(public dialogRef: MatDialogRef<ConfirmationComponent>) {}

  onNoClick(): void {
    this.dialogRef.close(false); // Return false if user cancels
  }

  onYesClick(): void {
    this.dialogRef.close(true); // Return true if user confirms
  }
}
