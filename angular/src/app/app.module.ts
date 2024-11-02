import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {HikingAlertsComponent} from './hiking-alerts/hiking-alerts.component';
import {AppRoutingModule} from './app.routing.module';
import {BrowserModule} from '@angular/platform-browser';
import {HikingMapComponent} from './hiking-alerts/hiking-map/hiking-map.component';
import {AlertListComponent} from './hiking-alerts/alert-list/alert-list.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {MatCardModule} from "@angular/material/card";
import {MatChipsModule} from "@angular/material/chips";
import {MatToolbarModule} from "@angular/material/toolbar";
import {HttpClientModule} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {CopyrightCreditsComponent} from './_footer/copyright-credits/copyright-credits.component';
import {PrivacyPolicyComponent} from './_footer/privacy-policy/privacy-policy.component';
import {TermsOfServiceComponent} from './_footer/terms-of-service/terms-of-service.component';
import {ContactComponent} from './_footer/contact/contact.component';

@NgModule({
  declarations: [
    AlertListComponent,
    AppComponent,
    ContactComponent,
    CopyrightCreditsComponent,
    HikingAlertsComponent,
    HikingMapComponent,
    PrivacyPolicyComponent,
    TermsOfServiceComponent
  ],
    imports: [
        BrowserModule.withServerTransition({appId: 'serverApp'}),
        AppRoutingModule,
        CommonModule,
        HttpClientModule,
        MatCardModule,
        MatChipsModule,
        MatToolbarModule
    ],
  bootstrap: [AppComponent],
  providers: [
    provideAnimationsAsync()
  ]
})
export class AppModule {}
