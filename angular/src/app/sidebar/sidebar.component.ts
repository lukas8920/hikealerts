import {Component, EventEmitter, Output} from '@angular/core';
import {SharedLogoutService} from '../shared-logout.service';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
  standalone: true,
  imports: [CommonModule, RouterModule]
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
