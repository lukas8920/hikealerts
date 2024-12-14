import {Component, EventEmitter, Output} from '@angular/core';
import {SharedListService} from '../shared.list.service';
import {Event} from '../../_service/event';
import {MatCardModule} from '@angular/material/card';
import {MatChipsModule} from '@angular/material/chips';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-alert-list',
  templateUrl: './alert-list.component.html',
  styleUrl: './alert-list.component.css',
  standalone: true,
  imports: [MatCardModule, MatChipsModule, CommonModule]
})
export class AlertListComponent {
  @Output() cardClick = new EventEmitter<{ lat: number, lng: number }>();
  eventList: any[] = [];  // Local list of objects to display

  constructor(private sharedList: SharedListService) {}

  ngOnInit(): void {
    // Subscribe to the shared service to receive the updated list of objects
    this.sharedList.eventList$.subscribe((newList) => {
      this.eventList = newList;  // Replace the current list with the new list
    });
  }

  isNullUrl(url: string): boolean {
    return url != null;
  }

  onCardClick(event: Event) {
    var lat = event.lat;
    var lng = event.lng;
    this.cardClick.emit({lat, lng});
  }
}
