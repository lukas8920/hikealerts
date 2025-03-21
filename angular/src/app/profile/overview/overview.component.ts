import {AfterViewInit, Component, Inject, Input, OnDestroy, PLATFORM_ID} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationComponent} from '../confirmation/confirmation.component';
import {UserService} from '../../_service/user.service';
import {SharedProfileService} from '../shared-profile.service';
import {Router} from '@angular/router';
import {SharedAppService} from '../../shared-app.service';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css',
  host: {class: 'closeToTop'}
})
export class OverviewComponent implements AfterViewInit, OnDestroy{
  profile: any;

  isApiKeyVisible: boolean = false;

  constructor(private dialog: MatDialog, private userService: UserService, private sharedDataService: SharedProfileService,
              private router: Router, @Inject(PLATFORM_ID) private platformId: any, private sharedAppService: SharedAppService) {
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
      this.sharedAppService.logout();
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

  ngOnDestroy(): void {
    this.sharedAppService.updateIsNavigating(false);
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.profile == null) {
        this.sharedAppService.updateIsNavigating(true);
      }
    }, 5);
    this.sharedDataService.data$.subscribe((data) => {
      setTimeout(() => this.profile = data);
      this.sharedAppService.updateIsNavigating(false);
    });
  }
}
