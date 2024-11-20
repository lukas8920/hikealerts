import {Component, EventEmitter, Inject, Output, PLATFORM_ID} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {UserService} from '../../_service/user.service';
import {isPlatformBrowser} from '@angular/common';
import {ErrorStateMatcher} from '@angular/material/core';
import {TokenStorageService} from '../../_service/token-storage.service';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.css'
})
export class LoginFormComponent {
  @Output() emitter = new EventEmitter();

  isError: boolean = false;
  form!: FormGroup;
  matcher: MyErrorStateMatcher;

  constructor(private fb: FormBuilder, private userService: UserService,
              private tokenStorage: TokenStorageService, private router: Router,
              @Inject(PLATFORM_ID) private platformId: any) {
    this.matcher = new MyErrorStateMatcher();
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', Validators.email],
      password: ['']
    });
  }

  resetPassword(): void {
    this.emitter.emit();
  }

  getMailErrorMessage(): String {
    return "Please enter a valid user name.";
  }

  getPwErrorMessage(): String {
    return "Invalid user credentials.";
  }

  onSubmit(form: FormGroup): void {
    this.userService.login(form.get('email')?.value, form.get('password')?.value)
      .subscribe(
        data => {
          this.isError = false;
          this.tokenStorage.saveToken(data.token);
          this.tokenStorage.saveUser(data);
          if (isPlatformBrowser(this.platformId)){
            this.router.navigateByUrl("/profile")
              .then(() => {window.location.reload()});
          }
        },
        error => {
          this.isError = true;
        }
      )
  }
}
