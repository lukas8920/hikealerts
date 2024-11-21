import {Component, EventEmitter, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {SharedAppService} from '../shared-app.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class SidebarComponent {
  @Output() closeEvent = new EventEmitter<void>();

  constructor(private sharedAppService: SharedAppService) {
  }

  getLoginStatus(): boolean {
    return  this.sharedAppService.isLoggedIn;
  }

  logout(): void {
    this.sharedAppService.logout();
    this.closeSidebar();
  }

  closeSidebar(): void{
    this.closeEvent.emit();
  }
}
