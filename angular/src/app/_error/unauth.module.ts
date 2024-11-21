import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {UnauthComponent} from './unauth/unauth.component';

const routes: Routes = [
  {path: '', component: UnauthComponent}
]

@NgModule({
  declarations: [UnauthComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class UnauthModule { }
