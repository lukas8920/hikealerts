import {Component} from '@angular/core';

@Component({
  selector: 'app-profile-nav',
  templateUrl: './profile-nav.component.html',
  styleUrl: './profile-nav.component.css'
})
export class ProfileNavComponent {
  isNavigation: boolean = true;

  constructor() {
  }

  ngOnInit(): void {
  }

  onClickExpandNavigation(): void {
    this.isNavigation = !this.isNavigation;
  }
}
