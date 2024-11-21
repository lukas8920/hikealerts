import {Component} from '@angular/core';
import {SharedAppService} from '../../shared-app.service';

@Component({
  selector: 'app-profile-nav',
  templateUrl: './profile-nav.component.html',
  styleUrl: './profile-nav.component.css'
})
export class ProfileNavComponent {
  isNavigation: boolean = true;
  isProfile: boolean = true;
  isMobile: boolean = false;

  constructor(private sharedAppService: SharedAppService) {
  }

  ngOnInit(): void {
    this.sharedAppService.isMobile$.subscribe(isMobile => {
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
