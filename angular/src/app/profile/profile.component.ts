import { Component } from '@angular/core';
import {Title} from '@angular/platform-browser';
import {Profile} from '../_service/profile';
import {UserService} from '../_service/user.service';
import {SharedProfileService} from './shared-profile.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
  constructor(private title: Title, private userService: UserService,
              private sharedProfile: SharedProfileService, private router: Router) {
    this.title.setTitle("Profile");
    this.router.navigateByUrl("/profile/(sub:overview)");

    this.userService.getProfile().subscribe(profile => {
      if (profile != null){
        this.sharedProfile.setData(profile);
      }
    })
  }
}
