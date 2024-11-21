import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {HikingAlertsComponent} from './hiking-alerts.component';
import {MatTooltipModule} from '@angular/material/tooltip';

const routes: Routes = [
  { path: '', component: HikingAlertsComponent }
]

@NgModule({
  declarations: [HikingAlertsComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatTooltipModule
  ]
})
export class HikingAlertsModule { }
