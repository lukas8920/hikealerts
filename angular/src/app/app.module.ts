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

@NgModule({
  declarations: [
    AlertListComponent,
    AppComponent,
    HikingAlertsComponent,
    HikingMapComponent
  ],
    imports: [
        BrowserModule.withServerTransition({appId: 'serverApp'}),
        AppRoutingModule,
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
