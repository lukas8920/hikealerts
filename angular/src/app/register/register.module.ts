import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RegisterFormComponent} from './register-form/register-form.component';
import {RegisterComponent} from './register.component';
import {RouterModule, Routes} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

const routes: Routes = [
  {path: '', component: RegisterComponent}
]

@NgModule({
  declarations: [
    RegisterFormComponent,
    RegisterComponent
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
export class RegisterModule { }
