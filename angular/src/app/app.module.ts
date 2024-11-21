import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {AppRoutingModule} from './app.routing.module';
import {BrowserModule} from '@angular/platform-browser';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {MatToolbarModule} from "@angular/material/toolbar";
import {HttpClientModule} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {authInterceptorProvider} from './auth.interceptor';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule.withServerTransition({appId: 'serverApp'}),
    AppRoutingModule,
    CommonModule,
    HttpClientModule,
    MatToolbarModule
  ],
  bootstrap: [AppComponent],
  providers: [
    authInterceptorProvider,
    provideAnimationsAsync()
  ]
})
export class AppModule {}
