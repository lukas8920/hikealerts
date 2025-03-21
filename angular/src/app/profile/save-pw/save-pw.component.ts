import { Component } from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {equivalentValidator} from '../../register/register-form/register-form.component';
import {UserService} from '../../_service/user.service';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}

@Component({
  selector: 'app-save-pw',
  templateUrl: './save-pw.component.html',
  styleUrl: './save-pw.component.css',
  host: {class: 'closeToTop'}
})
export class SavePwComponent {
  form: FormGroup;
  matcher: MyErrorStateMatcher;
  message: string = "";
  isValid: boolean = true;

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.matcher = new MyErrorStateMatcher();
    this.form = fb.group({
      password: ['', [this.validatePwLength]],
      password_check: ['', [this.validatePwLength]]
    }, {
      validator: equivalentValidator('password', 'password_check')
    });
    this.form.markAllAsTouched();
  }

  ngOnInit(): void {
  }

  isEmpty(): boolean {
    return this.message === "";
  }

  onSubmit(form: FormGroup): void {
    this.userService.savePw(form.get('password')?.value, "").subscribe(
      data => {
        this.message = data.message;
        this.form.reset();
      },
      error => {
        this.message = error.error;
        this.isValid = false;
        this.form.reset();
      }
    )
  }

  getPwErrorMessage(): string {
    return this.form.get('password')?.hasError('pwInvalidLength') ?
      'The password must have at least 8 characters.' : '';
  }

  getPwConfirmErrorMessage(): string {
    return this.form.get('password_check')?.hasError('pwInvalidLength') ?
      'The password must have at least 8 characters.' :
      (this.form.get('password_check')?.hasError('notEqual') ?
        'Both passwords need to match.' : '');
  }

  validatePwLength(control: AbstractControl): {[key: string]: any} | null  {
    if (control.value && control.value.length < 8) {
      control.setErrors({pwInvalidLength: true});
      return { pwInvalidLength: true };
    }
    control.setErrors(null);
    return null;
  }
}
