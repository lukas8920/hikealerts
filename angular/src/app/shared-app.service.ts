import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedAppService {
  private isMobileSource = new BehaviorSubject<boolean>(false);
  private isNavigatingSource = new BehaviorSubject<boolean>(false);

  isMobile$ = this.isMobileSource.asObservable();
  isNavigating$ = this.isNavigatingSource.asObservable();

  updateIsMobile(flag: boolean): void {
    this.isMobileSource.next(flag);
  }

  updateIsNavigating(flag: boolean): void {
    this.isNavigatingSource.next(flag);
  }
}
