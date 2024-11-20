import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DocumentationComponent} from './documentation.component';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    component: DocumentationComponent
  }
];

@NgModule({
  declarations: [DocumentationComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class DocumentationModule { }
