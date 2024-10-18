import {Component, OnInit} from '@angular/core';
import * as L from 'leaflet';
import 'leaflet.markercluster';
import {ApiService} from '../../_service/api.service';
import {Event} from '../../_service/event';
import {SharedListService} from '../shared.list.service';
import {Point} from 'leaflet';

@Component({
  selector: 'app-hiking-map',
  templateUrl: './hiking-map.component.html',
  styleUrl: './hiking-map.component.css'
})
export class HikingMapComponent implements OnInit {
  map: any;
  markerClusterGroup: any;

  private loadedMarkers: Map<string, Event> = new Map<string, Event>(); // Track loaded markers
  private offset = 0; // Initial offset for chunking
  private limit = 100; // Number of markers to fetch per request

  constructor(private apiService: ApiService, private sharedListService: SharedListService) {
  }

  ngOnInit(): void {
    this.initializeMap();
    this.fetchMarkers();
    this.updateVisibleMarkers();
    // Update visible markers when the map stops moving (panning or zooming)
    this.map.on('moveend', () => {
      this.updateVisibleMarkers();
    });
  }

  // Initialize the map
  initializeMap(): void {
    if (!this.map){
      this.map = L.map('map').setView([51.505, -0.09], 5); // Set initial center and zoom

      // Add OpenStreetMap tile layer
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(this.map);



      // Create the marker cluster group
      this.markerClusterGroup = L.markerClusterGroup({
        showCoverageOnHover: false,
        iconCreateFunction: function(cluster) {
          var count = cluster.getChildCount(); // Get number of markers in the cluster

          // Customize the cluster icon
          var icon = L.divIcon({
            html: '<div style="background-color: #cc3939d9; color: white; display: flex; justify-content: center; align-items: center; opacity: 0.95; font-size: 20px; width: 36px; height: 36px; border-radius: 50%;"><span>' + count + '</span></div>',
            className: 'marker-cluster',
            iconSize: [40, 40]
          });
          return icon;
        }
      });

      // Add the cluster group to the map
      this.map.addLayer(this.markerClusterGroup);
    }
  }

  fetchMarkers(): void {
    // Determine the offset based on current loaded markers
    this.apiService.getEvents(this.offset, this.limit).subscribe(events => {
      if (events.length === 0) return; // No more markers to load

      events.forEach(event => {
        const markerKey = `${event.lat}-${event.lng}`;

        // Check if this marker has already been loaded
        if (!this.loadedMarkers.has(markerKey)) {
          this.loadedMarkers.set(markerKey, event); // Mark this marker as loaded

          const iconSize = new Point(36,36); // Set the desired size for the icon
          const customIcon = L.divIcon({
            className: 'custom-marker', // Add a custom class for styling if needed
            html: '<div style="background-color: #cc3939d9; border: 5px solid transparent; font-size: 24px; color: white; border-radius: 50%; width: 36px; height: 36px; opacity: 0.95;"></div>',
            iconSize: iconSize,
            iconAnchor: [18, 18], // Anchor the icon to the center
          });

          const markerInstance = L.marker(L.latLng(event.lat, event.lng), {icon: customIcon})
            .addTo(this.markerClusterGroup);
          markerInstance.bindPopup(`${event.title}`);

          // Open popup on hover
          markerInstance.on('mouseover', function (e) {
            markerInstance.openPopup();
          });

          // Close popup when hover stops
          markerInstance.on('mouseout', function (e) {
            markerInstance.closePopup();
          });
        }
      });

      // Increment the offset for the next request
      this.offset += this.limit;
    });
  }

  updateVisibleMarkers(): void {
    const bounds = this.map.getBounds();
    const visibleMarkers: L.Marker[] = [];

    this.markerClusterGroup.eachLayer((layer: L.Marker) => {
      if (bounds.contains(layer.getLatLng())) {
        visibleMarkers.push(layer);
      }
    });

    const events = visibleMarkers
      .map(marker => `${marker.getLatLng().lat}-${marker.getLatLng().lng}`)
      .filter(markerKey => this.loadedMarkers.has(markerKey))
      .map(markerKey => this.loadedMarkers.get(markerKey));

    this.sharedListService.updateObjectList(events);
  }

  zoomToMarker(lat: number, lng: number) {
    this.map.setView(new L.LatLng(lat, lng), 15);
  }
}
