import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedOverlayService {
  private isOverlayVisibleSource = new BehaviorSubject<boolean>(false);
  private overlayEventSource = new BehaviorSubject<any>({});

  isOverlayVisible$ = this.isOverlayVisibleSource.asObservable();
  overlayEvent$ = this.overlayEventSource.asObservable();

  setOverlayVisibility(flag: boolean){
    this.isOverlayVisibleSource.next(flag);
  }

  updateOverlayEvent(event: any){
    this.overlayEventSource.next(event);
  }
}
