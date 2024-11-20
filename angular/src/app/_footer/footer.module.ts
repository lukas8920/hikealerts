import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes, UrlMatchResult, UrlSegment, UrlSegmentGroup} from '@angular/router';
import {CopyrightCreditsComponent} from './copyright-credits/copyright-credits.component';
import {PrivacyPolicyComponent} from './privacy-policy/privacy-policy.component';
import {TermsOfServiceComponent} from './terms-of-service/terms-of-service.component';
import {ContactComponent} from './contact/contact.component';

export const routes: Routes = [
  { path: 'contact', component: ContactComponent },
  { path: 'privacy-policy', component: PrivacyPolicyComponent },
  { path: 'terms-of-service', component: TermsOfServiceComponent },
  { path: 'copyright-credits', component: CopyrightCreditsComponent }
]

@NgModule({
  declarations: [
    PrivacyPolicyComponent,
    ContactComponent,
    TermsOfServiceComponent,
    CopyrightCreditsComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class FooterModule { }
