import {Inject, Injectable, PLATFORM_ID} from '@angular/core';
import {TokenStorageService} from './_service/token-storage.service';
import {Router} from '@angular/router';
import {isPlatformBrowser} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class SharedLogoutService {
  isLoggedIn: boolean = false

  constructor(private tokenStorageService: TokenStorageService, private router: Router,
              @Inject(PLATFORM_ID) private platformId: any) { }

  logout(): void {
    this.tokenStorageService.signOut();
    if (isPlatformBrowser(this.platformId)){
      this.router.navigateByUrl("/hiking-alerts")
        .then(() => {window.location.reload()});
    }
  }
}
