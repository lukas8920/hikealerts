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
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from '@angular/material/button';
import {RegisterComponent} from './register/register.component';
import {RegisterFormComponent} from './register/register-form/register-form.component';
import {UnauthComponent} from './_error/unauth/unauth.component';
import {ProfileComponent} from './profile/profile.component';
import {ProfileNavComponent} from './profile/profile-nav/profile-nav.component';
import {OverviewComponent} from './profile/overview/overview.component';
import {authInterceptorProvider} from './auth.interceptor';
import {ConfirmationComponent} from './profile/confirmation/confirmation.component';
import {MatDialogModule} from '@angular/material/dialog';
import {SavePwComponent} from './profile/save-pw/save-pw.component';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {OverlayEventComponent} from './hiking-alerts/overlay-event/overlay-event.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {SidebarComponent} from './sidebar/sidebar.component';

@NgModule({
  declarations: [
    AlertListComponent,
    AppComponent,
    ConfirmationComponent,
    ContactComponent,
    CopyrightCreditsComponent,
    HikingAlertsComponent,
    HikingMapComponent,
    OverlayEventComponent,
    OverviewComponent,
    PrivacyPolicyComponent,
    ProfileComponent,
    ProfileNavComponent,
    RegisterComponent,
    RegisterFormComponent,
    SavePwComponent,
    SidebarComponent,
    TermsOfServiceComponent,
    UnauthComponent
  ],
  imports: [
    BrowserModule.withServerTransition({appId: 'serverApp'}),
    AppRoutingModule,
    CommonModule,
    HttpClientModule,
    MatCardModule,
    MatChipsModule,
    MatToolbarModule,
    MatFormFieldModule, //tag
    MatInputModule, //tag
    MatButtonModule, //tag
    FormsModule, //tag
    ReactiveFormsModule, //tag
    MatDialogModule,
    MatSlideToggleModule,
    MatTooltipModule
  ],
  bootstrap: [AppComponent],
  providers: [
    authInterceptorProvider,
    provideAnimationsAsync()
  ]
})
export class AppModule {}
