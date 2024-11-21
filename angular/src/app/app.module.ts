import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {HikingAlertsComponent} from './hiking-alerts/hiking-alerts.component';
import {AppRoutingModule} from './app.routing.module';
import {BrowserModule} from '@angular/platform-browser';
import {AlertListComponent} from './hiking-alerts/alert-list/alert-list.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {MatCardModule} from "@angular/material/card";
import {MatChipsModule} from "@angular/material/chips";
import {MatToolbarModule} from "@angular/material/toolbar";
import {HttpClientModule} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {authInterceptorProvider} from './auth.interceptor';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {OverlayEventComponent} from './hiking-alerts/overlay-event/overlay-event.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {SidebarComponent} from './sidebar/sidebar.component';

@NgModule({
  declarations: [
    AlertListComponent,
    AppComponent,
    HikingAlertsComponent,
    OverlayEventComponent,
    SidebarComponent
  ],
  imports: [
    BrowserModule.withServerTransition({appId: 'serverApp'}),
    AppRoutingModule,
    CommonModule,
    HttpClientModule,
    MatCardModule,
    MatChipsModule,
    MatToolbarModule,
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
