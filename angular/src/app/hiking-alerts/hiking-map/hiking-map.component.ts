import {Component, Input, OnInit} from '@angular/core';
import * as L from 'leaflet';
import 'leaflet.markercluster';
import {ApiService} from '../../_service/api.service';
import {Event} from '../../_service/event';
import {SharedListService} from '../shared.list.service';
import {Point} from 'leaflet';
import * as pako from 'pako';
import {SharedOverlayService} from '../shared-overlay.service';

class CustomMarker extends L.Marker {
  id: string;

  constructor(latlng: L.LatLngExpression, options: L.MarkerOptions, id: string) {
    super(latlng, options);
    this.id = id;
  }
}

@Component({
  selector: 'app-hiking-map',
  templateUrl: './hiking-map.component.html',
  styleUrl: './hiking-map.component.css'
})
export class HikingMapComponent implements OnInit {
  @Input() isMobile = false;

  map: any;
  markerClusterGroup: any;

  private loadedMarkers: Map<string, Event> = new Map<string, Event>(); // Track loaded markers
  private offset = 0; // Initial offset for chunking
  private limit = 100; // Number of markers to fetch per request
  private leaflet = window.L;

  linestringLayers: Map<number, L.Polyline> = new Map();

  constructor(private apiService: ApiService, private sharedListService: SharedListService,
              private sharedOverlayService: SharedOverlayService) {
  }

  ngOnInit(): void {
    this.initializeMap();
    this.fetchMarkers();

    // Update visible markers when the map stops moving (panning or zooming)
    this.map.on('moveend', () => {
      this.updateVisibleMarkers(false);
    });
    // Fetch the GeoJSON data and add it to the map
    this.apiService.getGeoJsonLayer().subscribe(geoJSON => this.addGeoJsonData(geoJSON))
  }

  // Initialize the map
  initializeMap(): void {
    if (!this.map){
      this.map = this.leaflet.map('map', {
        worldCopyJump: true,  // Enable horizontal wrapping
        maxBoundsViscosity: 1.0 // Prevents bouncing at the vertical edge
      }).setView([51.505, -0.09], 2); // Set initial center and zoom

      // Set vertical bounds (latitude limits only)
      const southWest = L.latLng(-85, -Infinity);
      const northEast = L.latLng(85, Infinity);
      const bounds = L.latLngBounds(southWest, northEast);

      this.map.setMaxBounds(bounds);

      // Add OpenStreetMap tile layer
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(this.map);

      // Create the marker cluster group
      this.markerClusterGroup = this.leaflet.markerClusterGroup({
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

  addGeoJsonData(geoJsonData: any): void {
    const decompressedString = pako.inflate(new Uint8Array(geoJsonData), { to: 'string' });
    const decompressedGeoJsonData = JSON.parse(decompressedString) as GeoJSON.FeatureCollection
    const geoJsonLayer =  L.geoJSON(decompressedGeoJsonData, {
      style: {
        color: 'red',
        weight: 2,
      },
      onEachFeature: (feature, layer) => {
        // add trail id to the reference map for the markers
        const id = feature.properties.id;
        if (layer instanceof L.Polyline) {
          this.linestringLayers.set(id, layer);
        }

        // Show the 'name' property on hover
        layer.on('mouseover', (e) => {
          const tooltip = L.tooltip()
            .setContent(feature.properties.trail_name)
            .setLatLng(e.latlng)
            .addTo(this.map);
          layer.on('mouseout', () => {
            this.map.removeLayer(tooltip);
          });
        });
      }
    });
    geoJsonLayer.addTo(this.map);
  }

  fetchMarkers(): void {
    const self = this;
    this.apiService.getEvents(this.offset, this.limit).subscribe(events =>
      this.processResponse(events, self), error => this.updateVisibleMarkers(true), () => this.updateVisibleMarkers(true));
  }

  processResponse(events: Event[], self: HikingMapComponent): void {
    if (events.length === 0) {
      // No more markers to load, so update map
      return;
    }

    events.forEach(event => {
      const markerKey = event.id.toString();
      event.create_date = event.create_date.split(" ")[0];

      // Check if this marker has already been loaded
      if (!this.loadedMarkers.has(markerKey)) {
        this.loadedMarkers.set(markerKey, event); // Mark this marker as loaded

        const iconSize = new Point(36,36); // Set the desired size for the icon
        const customIcon = this.leaflet.divIcon({
          className: 'custom-marker', // Add a custom class for styling if needed
          html: '<div style="background-color: #cc3939d9; border: 5px solid transparent; font-weight: bold; font-size: 36px; display: flex; justify-content: center; align-items: center;  color: white; border-radius: 50%; width: 36px; height: 36px; opacity: 0.95;">!</div>',
          iconSize: iconSize,
          iconAnchor: [18, 18], // Anchor the icon to the center
        });

        const markerInstance = new CustomMarker(this.leaflet.latLng(event.lat, event.lng), {icon: customIcon}, markerKey).addTo(this.markerClusterGroup);
        markerInstance.bindPopup(`${event.title}`);

        // Open popup on hover
        markerInstance.on('mouseover', function () {
          event.trail_ids.forEach(lineId => {
            const lineLayer = self.linestringLayers.get(lineId);
            if (lineLayer) {
              lineLayer.setStyle({ color: 'blue' }); // Highlight color
            }
          });
          markerInstance.openPopup();
        });

        // Close popup when hover stops
        markerInstance.on('mouseout', function () {
          event.trail_ids.forEach(lineId => {
            const lineLayer = self.linestringLayers.get(lineId);
            if (lineLayer) {
              lineLayer.setStyle({ color: 'red' }); // Original color
            }
          });
          markerInstance.closePopup();
        });

        //on click if mobile open overlay event
        markerInstance.on('click', function () {
          if (self.isMobile){
            self.sharedOverlayService.updateOverlayEvent(event);
            self.sharedOverlayService.setOverlayVisibility(true);
          }
        });
      }
    });

    // Increment the offset for the next request
    this.offset += this.limit;
    this.apiService.getEvents(this.offset, this.limit).subscribe(events =>
      this.processResponse(events, self));
  }

  //init and
  updateVisibleMarkers(isInit: boolean): void {
    if (isInit || !this.isMobile){
      const bounds = this.map.getBounds();
      const visibleMarkers: L.Marker[] = [];

      this.markerClusterGroup.eachLayer((layer: L.Marker) => {
        if (bounds.contains(layer.getLatLng())) {
          visibleMarkers.push(layer);
        }
      });

      const events = visibleMarkers
        .map(marker => {
          if (marker instanceof CustomMarker){
            return marker.id ?? "";
          }
          return "";
        })
        .filter(markerKey => this.loadedMarkers.has(markerKey))
        .map(markerKey => this.loadedMarkers.get(markerKey));

      this.sharedListService.updateObjectList(events);
    }
  }

  zoomToMarker(lat: number, lng: number) {
    this.map.setView(new this.leaflet.LatLng(lat, lng), 15);
  }
}
