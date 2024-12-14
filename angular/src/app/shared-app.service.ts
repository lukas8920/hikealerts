import {Inject, Injectable, PLATFORM_ID} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {TokenStorageService} from './_service/token-storage.service';
import {Router} from '@angular/router';
import {isPlatformBrowser} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class SharedAppService {
  private isMobileSource = new BehaviorSubject<boolean>(false);
  private isNavigatingSource = new BehaviorSubject<boolean>(false);

  isLoggedIn: boolean = false

  constructor(private tokenStorageService: TokenStorageService, private router: Router,
              @Inject(PLATFORM_ID) private platformId: any) { }

  isMobile$ = this.isMobileSource.asObservable();
  isNavigating$ = this.isNavigatingSource.asObservable();

  updateIsMobile(flag: boolean): void {
    this.isMobileSource.next(flag);
  }

  updateIsNavigating(flag: boolean): void {
    this.isNavigatingSource.next(flag);
  }

  logout(): void {
    this.tokenStorageService.signOut();
    if (isPlatformBrowser(this.platformId)){
      this.router.navigateByUrl("/hiking-alerts")
        .then(() => {window.location.reload()});
    }
    //todo end signalr connection
  }
}
