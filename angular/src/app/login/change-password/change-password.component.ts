import {Component, Inject, PLATFORM_ID} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {isPlatformBrowser} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {equivalentValidator} from '../../register/register-form/register-form.component';
import {UserService} from '../../_service/user.service';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent {
  form: FormGroup;
  matcher: MyErrorStateMatcher;
  token: string | null = "";

  constructor(private fb: FormBuilder, private route: ActivatedRoute, private userService: UserService,
              private router: Router, @Inject(PLATFORM_ID) private platformId: any) {
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
    this.token = this.route.snapshot.paramMap.get('id');
  }

  onSubmit(form: FormGroup): void {
    this.userService.changePw(form.get('password')?.value, this.token).subscribe(
      data => {
        console.log
        if (isPlatformBrowser(this.platformId)){
          this.router.navigateByUrl("/login")
            .then(() => {window.location.reload()});
        }
      },
      error => {
        console.log(error.message)
        this.router.navigateByUrl('/unauth');
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
