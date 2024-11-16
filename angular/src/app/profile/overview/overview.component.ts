import {Component, Inject, Input, PLATFORM_ID} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationComponent} from '../confirmation/confirmation.component';
import {UserService} from '../../_service/user.service';
import {SharedProfileService} from '../shared-profile.service';
import {Router} from '@angular/router';
import {SharedLogoutService} from '../../shared-logout.service';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css'
})
export class OverviewComponent {
  profile: any;

  isApiKeyVisible: boolean = false;

  constructor(private dialog: MatDialog, private userService: UserService, private sharedDataService: SharedProfileService,
              private router: Router, @Inject(PLATFORM_ID) private platformId: any, private sharedLogoutService: SharedLogoutService) {
  }

  ngOnInit() {
    this.sharedDataService.data$.subscribe((data) => {
      this.profile = data;
    });
  }

  openConfirmationDialog(): void {
    const dialogRef = this.dialog.open(ConfirmationComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // User clicked "Yes"
        console.log("Request account deletion");
        this.deleteAccount();
      } else {
        // User clicked "No" or closed the dialog
        console.log('User cancelled the action');
      }
    });
  }

  deleteAccount(){
    this.userService.deleteAccount().subscribe(data => {
      console.log(data);
      this.sharedLogoutService.logout();
    }, error => console.log(error));
  }

  refreshApiKey(){
    this.userService.refreshApiKey().subscribe(data => {
      this.profile.api_key = data.message;
      this.isApiKeyVisible = true;
    }, error => {
      console.log(error);
    });
  }

  isKeyVisible(): boolean {
    return this.profile?.api_key.length > 35;
  }
}
