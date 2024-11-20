import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {LoginComponent} from './login.component';
import {ResetPasswordComponent} from './reset-password/reset-password.component';
import {LoginFormComponent} from './login-form/login-form.component';
import {ChangePasswordComponent} from './change-password/change-password.component';
import {RouterModule, Routes} from '@angular/router';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

const routes: Routes = [
  {path: '', component: LoginComponent},
  { path: 'change-password/:id', component: ChangePasswordComponent}
]

@NgModule({
  declarations: [
    LoginComponent,
    ResetPasswordComponent,
    LoginFormComponent,
    ChangePasswordComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class LoginModule { }
