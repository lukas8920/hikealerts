import { Component } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  FormGroupDirective,
  NgForm,
  Validators
} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {UserService} from '../../_service/user.service';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}

export const equivalentValidator = (firstControlName: string, secondControlName: string) => {
  return (formGroup: FormGroup) => {
    const firstControl = formGroup.get(firstControlName);
    const secondControl = formGroup.get(secondControlName);
    // @ts-ignore
    if (firstControl.value !== secondControl.value) {
      // @ts-ignore
      return secondControl.setErrors({ notEqual: true });
    }
    return secondControl?.setErrors(null);
  }
}

@Component({
  selector: 'app-register-form',
  templateUrl: './register-form.component.html',
  styleUrl: './register-form.component.css'
})
export class RegisterFormComponent {
  isSuccessful: boolean = false;
  isError: boolean = false;
  errorMessage: String = "";
  responseMessage: String = "";
  isSubmitted = false;

  form!: FormGroup;
  matcher: ErrorStateMatcher;

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.matcher = new MyErrorStateMatcher();
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.email]],
      password: ['', [this.validatePwLength]],
      password_check: ['',[this.validatePwLength]]
    }, {
      validator: equivalentValidator('password', 'password_check'),
    });
    this.form.markAllAsTouched();
  }

  onSubmit(form: FormGroup) {
    this.isSubmitted = true;
    this.userService.register(form.get('email')?.value, form.get('password')?.value)
      .subscribe(
        data => {
          console.log(data);
          this.isSuccessful = true;
          this.responseMessage = data.message;
        },
        err => {
          this.errorMessage = err.error;
          this.isError = true;
        }
      )
  }

  getPwConfirmErrorMessage() : String  {
    return this.form.get('password_check')?.hasError('pwInvalidLength') ?
      'The password must have at least 8 characters.' :
      (this.form.get('password_check')?.hasError('notEqual') ?
        'Both passwords need to match.' : '');
  }

  getPwErrorMessage() : String{
    return this.form.get('password')?.hasError('pwInvalidLength') ?
      'The password must have at least 8 characters.' : '';
  }

  getMailErrorMessage(): String {
    return "Please enter a valid email.";
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
