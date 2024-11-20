import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {HikingAlertsComponent} from './hiking-alerts/hiking-alerts.component';
import {CopyrightCreditsComponent} from './_footer/copyright-credits/copyright-credits.component';
import {ContactComponent} from './_footer/contact/contact.component';
import {PrivacyPolicyComponent} from './_footer/privacy-policy/privacy-policy.component';
import {TermsOfServiceComponent} from './_footer/terms-of-service/terms-of-service.component';
import {UnauthComponent} from './_error/unauth/unauth.component';
import {ProfileComponent} from './profile/profile.component';
import {OverviewComponent} from './profile/overview/overview.component';
import {SavePwComponent} from './profile/save-pw/save-pw.component';

const routes: Routes = [
  { path: '',   redirectTo: '/hiking-alerts', pathMatch: 'full' },
  { path: 'copyright-credits', component: CopyrightCreditsComponent},
  { path: 'contact', component: ContactComponent},
  { path: 'privacy-policy', component: PrivacyPolicyComponent},
  { path: 'terms-of-service', component: TermsOfServiceComponent},
  { path: 'hiking-alerts', component: HikingAlertsComponent},
  { path: 'login', loadChildren: () => import('./login/login.module').then(m => m.LoginModule)},
  { path: 'register', loadChildren: () => import('./register/register.module').then(m => m.RegisterModule)},
  { path: 'unauth', component: UnauthComponent},
  {path: 'profile', component: ProfileComponent, children: [
      {path:'', redirectTo: '/profile/(sub:overview)', pathMatch: 'full'},
      {path: 'overview', component: OverviewComponent, outlet: 'sub'},
      {path: 'change-password', component: SavePwComponent, outlet: 'sub'},
    ]},
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
