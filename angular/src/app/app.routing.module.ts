import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {HikingAlertsComponent} from './hiking-alerts/hiking-alerts.component';

const routes: Routes = [
  { path: '',   redirectTo: '/hiking-alerts', pathMatch: 'full' },
  { path: 'hiking-alerts', component: HikingAlertsComponent}
]
const routerOptions: ExtraOptions = {
  scrollPositionRestoration: "enabled",
  anchorScrolling: 'enabled'
}

@NgModule({
  imports: [RouterModule.forRoot(routes, routerOptions)],
  exports: [RouterModule]
})
export class AppRoutingModule{}
