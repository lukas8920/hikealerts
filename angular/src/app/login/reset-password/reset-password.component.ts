import { Component } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm, Validators} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {UserService} from '../../_service/user.service';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent {
  matcher: MyErrorStateMatcher;
  form: FormGroup;
  isForm: boolean = true;
  message: string = "";

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.matcher = new MyErrorStateMatcher();
    this.form = this.fb.group({
      email: new FormControl('', [Validators.email])
    })
  }

  ngOnInit(): void {
  }

  getMailErrorMessage(): String {
    return "Please enter a username.";
  }

  onSubmit(form: FormGroup): void{
    this.userService.resetPw(form.get('email')?.value).subscribe(
      data => {
        console.log(data);
        this.isForm = false;
        this.message = data.message;
      }
    )
  }
}
