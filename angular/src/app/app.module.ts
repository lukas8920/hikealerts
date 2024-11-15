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
import {LoginFormComponent} from './login/login-form/login-form.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from '@angular/material/button';
import {LoginComponent} from './login/login.component';
import {ResetPasswordComponent} from './login/reset-password/reset-password.component';
import {RegisterComponent} from './register/register.component';
import {RegisterFormComponent} from './register/register-form/register-form.component';
import {UnauthComponent} from './_error/unauth/unauth.component';
import {ChangePasswordComponent} from './login/change-password/change-password.component';
import {ProfileComponent} from './profile/profile.component';
import {ProfileNavComponent} from './profile/profile-nav/profile-nav.component';
import {OverviewComponent} from './profile/overview/overview.component';
import {authInterceptorProvider} from './auth.interceptor';
import {ConfirmationComponent} from './profile/confirmation/confirmation.component';
import {MatDialogModule} from '@angular/material/dialog';
import {SavePwComponent} from './profile/save-pw/save-pw.component';
import {DocumentationComponent} from './documentation/documentation.component';

@NgModule({
  declarations: [
    AlertListComponent,
    AppComponent,
    ChangePasswordComponent,
    ConfirmationComponent,
    ContactComponent,
    CopyrightCreditsComponent,
    DocumentationComponent,
    HikingAlertsComponent,
    HikingMapComponent,
    LoginComponent,
    LoginFormComponent,
    OverviewComponent,
    PrivacyPolicyComponent,
    ProfileComponent,
    ProfileNavComponent,
    RegisterComponent,
    RegisterFormComponent,
    ResetPasswordComponent,
    SavePwComponent,
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
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule
  ],
  bootstrap: [AppComponent],
  providers: [
    authInterceptorProvider,
    provideAnimationsAsync()
  ]
})
export class AppModule {}
