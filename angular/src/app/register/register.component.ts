import { Component } from '@angular/core';
import {Meta, Title} from '@angular/platform-browser';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  constructor(private title: Title, private meta: Meta) {
    this.title.setTitle("Join Hiking Alerts");
    this.meta.updateTag({name: 'Description', content: 'Sign up for Hiking Alerts to contribute alerts and use the API.'});
  }
}
