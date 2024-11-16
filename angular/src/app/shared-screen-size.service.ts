import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedScreenSizeService {
  private isMobileSource = new BehaviorSubject<boolean>(false);

  isMobile$ = this.isMobileSource.asObservable();

  updateIsMobile(flag: boolean): void {
    this.isMobileSource.next(flag);
  }
}
