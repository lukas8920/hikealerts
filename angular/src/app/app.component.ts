import {Component} from '@angular/core';
import {TokenStorageService} from './_service/token-storage.service';
import {SharedLogoutService} from './shared-logout.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  constructor(private tokenStorageService: TokenStorageService, private sharedLogoutService: SharedLogoutService) {
  }

  ngOnInit(): void {
    this.sharedLogoutService.isLoggedIn = !!this.tokenStorageService.getToken();

    if (this.sharedLogoutService.isLoggedIn){
      this.tokenStorageService.getUser();
    }
  }

  getLoginStatus(): boolean{
    return this.sharedLogoutService.isLoggedIn;
  }

  logout(): void{
    this.sharedLogoutService.logout();
  }
}
