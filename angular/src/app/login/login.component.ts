import { Component } from '@angular/core';
import {Meta, Title} from '@angular/platform-browser';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  showReset: Boolean = false;

  constructor(private title: Title, private meta: Meta) {
    this.title.setTitle("Login");
    this.meta.updateTag({name: 'Description', content: 'Log into your Hiking Alerts Account to manage your access credentials for the Hiking Alerts API.'})
  }

  ngOnInit(): void {
  }

  resetPassword(event: Event): void {
    this.showReset = true;
  }
}
