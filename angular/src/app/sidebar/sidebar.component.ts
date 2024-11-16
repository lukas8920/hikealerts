import {Component, EventEmitter, Output} from '@angular/core';
import {SharedLogoutService} from '../shared-logout.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  @Output() closeEvent = new EventEmitter<void>();

  constructor(private sharedLogoutService: SharedLogoutService) {
  }

  getLoginStatus(): boolean {
    return  this.sharedLogoutService.isLoggedIn;
  }

  logout(): void {
    this.sharedLogoutService.logout();
    this.closeSidebar();
  }

  closeSidebar(): void{
    this.closeEvent.emit();
  }
}
