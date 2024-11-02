import {ExtraOptions, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {HikingAlertsComponent} from './hiking-alerts/hiking-alerts.component';
import {CopyrightCreditsComponent} from './_footer/copyright-credits/copyright-credits.component';
import {ContactComponent} from './_footer/contact/contact.component';
import {PrivacyPolicyComponent} from './_footer/privacy-policy/privacy-policy.component';
import {TermsOfServiceComponent} from './_footer/terms-of-service/terms-of-service.component';

const routes: Routes = [
  { path: '',   redirectTo: '/hiking-alerts', pathMatch: 'full' },
  {path: 'copyright-credits', component: CopyrightCreditsComponent},
  { path: 'contact', component: ContactComponent},
  { path: 'privacy-policy', component: PrivacyPolicyComponent},
  { path: 'terms-of-service', component: TermsOfServiceComponent},
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
