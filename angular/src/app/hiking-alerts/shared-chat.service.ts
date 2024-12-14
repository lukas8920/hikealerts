import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedChatService {
  private isChatVisibleSource = new BehaviorSubject<boolean>(false);
  private initialMessagesSource = new BehaviorSubject<any>(null);
  private hasUnreadMessagesSource = new BehaviorSubject<number>(0);

  connection: any;

  isChatVisible$ = this.isChatVisibleSource.asObservable();
  initialMessages$ = this.initialMessagesSource.asObservable();
  hasUnreadMessages$ = this.hasUnreadMessagesSource.asObservable();

  setChatVisibility(flag: boolean){
    this.isChatVisibleSource.next(flag);
  }

  setUnreadMessages(count: number){
    this.hasUnreadMessagesSource.next(count);
  }

  setInitialMessages(messages: any){
    this.initialMessagesSource.next(messages);
  }

  incrementCounter(): void {
    this.hasUnreadMessagesSource.next(this.hasUnreadMessagesSource.getValue() + 1);
  }
}
