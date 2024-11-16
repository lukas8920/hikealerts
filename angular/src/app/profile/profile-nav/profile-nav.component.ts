import {Component} from '@angular/core';
import {SharedScreenSizeService} from '../../shared-screen-size.service';

@Component({
  selector: 'app-profile-nav',
  templateUrl: './profile-nav.component.html',
  styleUrl: './profile-nav.component.css'
})
export class ProfileNavComponent {
  isNavigation: boolean = true;
  isProfile: boolean = true;
  isMobile: boolean = false;

  constructor(private sharedScreenSize: SharedScreenSizeService) {
  }

  ngOnInit(): void {
    this.sharedScreenSize.isMobile$.subscribe(isMobile => {
      if (this.isMobile != isMobile){
        this.isNavigation = !isMobile;
      }
      this.isMobile = isMobile;
    });
  }

  onClickExpandNavigation(): void {
    this.isNavigation = !this.isNavigation;
  }

  onClickExpandProfile(): void {
    this.isProfile = !this.isProfile;
  }
}
