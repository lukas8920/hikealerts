import {Component, OnInit, Renderer2} from '@angular/core';
import {latLng, latLngBounds, LatLngExpression, Marker, MarkerOptions, Polyline, tileLayer} from 'leaflet';
import 'leaflet.vectorgrid';
import 'leaflet.markercluster';
import {ApiService} from '../../_service/api.service';
import {Event} from '../../_service/event';
import {SharedListService} from '../shared.list.service';
import {SharedOverlayService} from '../shared-overlay.service';
import {SharedAppService} from '../../shared-app.service';
import {Icon} from './icon';

class CustomMarker extends Marker {
  id: string;

  constructor(latlng: LatLngExpression, options: MarkerOptions, id: string) {
    super(latlng, options);
    this.id = id;
  }
}

@Component({
  selector: 'app-hiking-map',
  templateUrl: './hiking-map.component.html',
  styleUrl: './hiking-map.component.css',
  standalone: true
})
export class HikingMapComponent implements OnInit {
  vectorTileUrl = 'https://hiking-alerts.org:8080/v1/tiles/{z}/{x}/{y}.pbf';

  vectorGridLayer: any;
  map: any;
  markerClusterGroup: any;
  currentTooltip: any;

  private loadedMarkers: Map<string, Event> = new Map<string, Event>(); // Track loaded markers
  private offset = 0; // Initial offset for chunking
  private limit = 100; // Number of markers to fetch per request
  private leaflet = window.L;
  private icon;

  private isMobile = false;

  isNavigating = false;

  linestringLayers: Map<number, Polyline> = new Map();

  constructor(private apiService: ApiService, private sharedListService: SharedListService, private renderer: Renderer2,
              private sharedOverlayService: SharedOverlayService, private sharedAppService: SharedAppService) {
    this.icon = new Icon(this.leaflet);
  }

  ngOnInit(): void {
    this.loadCSSFiles();
    this.loadScripts();

    this.initializeMap();
    this.fetchMarkers();

    this.addVectorTiles();

    this.sharedAppService.isMobile$.subscribe(isMobile => {
      this.isMobile = isMobile;
    });
    this.sharedAppService.isNavigating$.subscribe(isNavigating => this.isNavigating = isNavigating, error => console.log(error));

    // Update visible markers when the map stops moving (panning or zooming)
    this.map.on('moveend', () => this.updateVisibleMarkers(false));
  }

  loadCSSFiles(){
    this.addStylesheets([
      'assets/MarkerCluster.css',
      'assets/MarkerCluster.Default.css',
      'assets/leaflet.css'
    ]);
  }

  loadScripts(){
    this.addScripts([
      {scriptUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet.markercluster/1.5.3/leaflet.markercluster.js', integrity: 'sha512-OFs3W4DIZ5ZkrDhBFtsCP6JXtMEDGmhl0QPlmWYBJay40TT1n3gt2Xuw8Pf/iezgW9CdabjkNChRqozl/YADmg=='},
      {scriptUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js', integrity: 'sha512-puJW3E/qXDqYp9IfhAI54BJEaWIfloJ7JWs7OeD5i6ruC9JZL1gERT1wjtwXFlh7CjE7ZJ+/vcRZRkIYIb6p4g=='},
      {scriptUrl: 'https://unpkg.com/leaflet.vectorgrid@1.3.0/dist/Leaflet.VectorGrid.bundled.min.js', integrity: null}
    ]);
  }

  addStylesheets(filePaths: string[]) {
    filePaths.forEach((path) => {
      const link = this.renderer.createElement('link');
      link.rel = 'stylesheet';
      link.href = path;
      this.renderer.appendChild(document.head, link);
    });
  }

  private addScripts(scripts: {scriptUrl: string, integrity: string | null}[]): void {
    scripts.forEach((s) => {
      if (!this.isScriptLoaded(s.scriptUrl)) {
        const script = this.renderer.createElement('script');
        script.type = 'text/javascript';
        script.src = s.scriptUrl;
        if (s.integrity != null){
          script.integrity = s.integrity;
        }
        script.async = true;
        script.defer = true;
        this.renderer.appendChild(document.body, script);
      }
    });
  }

  private isScriptLoaded(src: string): boolean {
    return Array.from(document.getElementsByTagName('script')).some(
      script => script.src.includes(src)
    );
  }

  // Initialize the map
  initializeMap(): void {
    if (!this.map){
      this.map = this.leaflet.map('map', {
        worldCopyJump: true,  // Enable horizontal wrapping
        maxBoundsViscosity: 1.0 // Prevents bouncing at the vertical edge
      }).setView([51.505, -0.09], 2); // Set initial center and zoom

      // Set vertical bounds (latitude limits only)
      const southWest = latLng(-85, -Infinity);
      const northEast = latLng(85, Infinity);
      const bounds = latLngBounds(southWest, northEast);

      this.map.setMaxBounds(bounds);

      // Add OpenStreetMap tile layer
      tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 13,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(this.map);

      // Create the marker cluster group
      var self = this;
      this.markerClusterGroup = this.leaflet.markerClusterGroup({
        showCoverageOnHover: false,
        iconCreateFunction: function(cluster) {
          var count = cluster.getChildCount(); // Get number of markers in the cluster
          return self.icon.getClusterIcon(count);
        }
      });

      // Add the cluster group to the map
      this.map.addLayer(this.markerClusterGroup);
      this.map.on('zoomend', () => this.addVectorTiles());
    }
  }

  addVectorTiles(): void {
    const self = this;
    // Check if zoom level is between 12 and 15
    var zoomLevel = this.map.getZoom();
    if (zoomLevel >= 7 && zoomLevel <= 13) {
      // If the vector grid layer doesn't exist yet, add it
      if (!this.vectorGridLayer) {
        this.vectorGridLayer = (this.leaflet as any).vectorGrid.protobuf(this.vectorTileUrl, {
          minZoom: 7,
          vectorTileLayerStyles: {
            'trail_layer': {
              color: 'red',
              weight: 2
            }
          },
          interactive: true,
          getFeatureId: (feature: any) => feature.properties.id
        }).addTo(this.map);
        this.vectorGridLayer.on('mouseover', function(e: any) {
          var properties = e.layer.properties;  // Access feature properties (tags)
          console.log("mouseover");

          // Example: Show a popup with a specific tag value
          var name = properties.name || 'Unknown';
          self.currentTooltip = self.leaflet.tooltip()
            .setContent(name)
            .setLatLng(e.latlng)
            .addTo(self.map);
        });
        this.vectorGridLayer.on('mouseout', () => {
          if (self.currentTooltip){
            self.map.removeLayer(self.currentTooltip);
            self.currentTooltip = null;
          }
        });
      }
    } else {
      // Remove the vector grid layer if zoom level is outside range
      if (this.vectorGridLayer) {
        this.map.removeLayer(this.vectorGridLayer);
        this.vectorGridLayer = null;
        self.currentTooltip = null;
      }
    }
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

        const markerInstance = new CustomMarker(this.leaflet.latLng(event.lat, event.lng), {icon: this.icon.getMainIcon()}, markerKey).addTo(this.markerClusterGroup);
        markerInstance.bindPopup(`${event.title}`);

        // Open popup on hover
        markerInstance.on('mouseover', function () {
          event.trail_ids.forEach(lineId => {
            self.vectorGridLayer.setFeatureStyle(lineId, {
              color: 'blue'
            });
          });
          markerInstance.openPopup();
        });

        // Close popup when hover stops
        markerInstance.on('mouseout', function () {
          event.trail_ids.forEach(lineId => {
            self.vectorGridLayer.resetFeatureStyle(lineId)
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
      const visibleMarkers: Marker[] = [];

      this.markerClusterGroup.eachLayer((layer: Marker) => {
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
