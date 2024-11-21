import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {HikingAlertsComponent} from './hiking-alerts/hiking-alerts.component';

const routes: Routes = [
  { path: '',   redirectTo: '/hiking-alerts', pathMatch: 'full'},
  { path: 'footer', loadChildren: () => import('./_footer/footer.module').then(m => m.FooterModule)},
  { path: 'hiking-alerts', component: HikingAlertsComponent},
  { path: 'login', loadChildren: () => import('./login/login.module').then(m => m.LoginModule)},
  { path: 'register', loadChildren: () => import('./register/register.module').then(m => m.RegisterModule)},
  { path: 'unauth', loadChildren: () => import('./_error/unauth.module').then(m => m.UnauthModule)},
  { path: 'profile', loadChildren: () => import('./profile/profile.module').then(m => m.ProfileModule)},
  {path: 'specs', loadChildren: () => import('./documentation/documentation.module').then(m => m.DocumentationModule)}
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
