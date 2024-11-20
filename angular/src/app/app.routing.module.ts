import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {HikingAlertsComponent} from './hiking-alerts/hiking-alerts.component';
import {CopyrightCreditsComponent} from './_footer/copyright-credits/copyright-credits.component';
import {ContactComponent} from './_footer/contact/contact.component';
import {PrivacyPolicyComponent} from './_footer/privacy-policy/privacy-policy.component';
import {TermsOfServiceComponent} from './_footer/terms-of-service/terms-of-service.component';
import {LoginComponent} from './login/login.component';
import {RegisterComponent} from './register/register.component';
import {UnauthComponent} from './_error/unauth/unauth.component';
import {ChangePasswordComponent} from './login/change-password/change-password.component';
import {ProfileComponent} from './profile/profile.component';
import {OverviewComponent} from './profile/overview/overview.component';
import {SavePwComponent} from './profile/save-pw/save-pw.component';

const routes: Routes = [
  { path: '',   redirectTo: '/hiking-alerts', pathMatch: 'full' },
  {path: 'copyright-credits', component: CopyrightCreditsComponent},
  { path: 'contact', component: ContactComponent},
  { path: 'privacy-policy', component: PrivacyPolicyComponent},
  { path: 'terms-of-service', component: TermsOfServiceComponent},
  { path: 'hiking-alerts', component: HikingAlertsComponent},
  { path: 'login', component: LoginComponent},
  { path: 'register', component: RegisterComponent},
  { path: 'unauth', component: UnauthComponent},
  { path: 'change-password/:id', component: ChangePasswordComponent},
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
