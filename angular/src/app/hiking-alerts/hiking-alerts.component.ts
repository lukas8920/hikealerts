import {Component, ViewChild} from '@angular/core';
import {HikingMapComponent} from './hiking-map/hiking-map.component';

@Component({
  selector: 'app-hiking-alerts',
  templateUrl: './hiking-alerts.component.html',
  styleUrl: './hiking-alerts.component.css'
})
export class HikingAlertsComponent {
  @ViewChild('hikingMapComponent') hikingMapComponent!: HikingMapComponent;

  // Handle card click and pass the coordinates to Leaflet map
  onCardClick(coordinates: { lat: number, lng: number }) {
    this.hikingMapComponent?.zoomToMarker(coordinates.lat, coordinates.lng);
  }
}
