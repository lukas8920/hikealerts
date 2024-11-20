import {AfterViewInit, Component, Injector, OnInit, ViewChild, ViewContainerRef} from '@angular/core';
import {SharedOverlayService} from './shared-overlay.service';
import {Event} from '../_service/event';
import {SharedScreenSizeService} from '../shared-screen-size.service';

@Component({
  selector: 'app-hiking-alerts',
  templateUrl: './hiking-alerts.component.html',
  styleUrl: './hiking-alerts.component.css'
})
export class HikingAlertsComponent implements OnInit, AfterViewInit {
  @ViewChild('container', { read: ViewContainerRef }) container!: ViewContainerRef;

  showMap: boolean = true;
  isMobile: boolean = false;
  isOverlayVisible: boolean = false;
  isOverlayOpening: boolean = true;

  event: Event | null = null;

  componentRef: any;

  constructor(private sharedOverlayService: SharedOverlayService, private sharedScreenSize: SharedScreenSizeService,
              private injector: Injector) {
  }

  // Handle card click and pass the coordinates to Leaflet map
  onCardClick(coordinates: { lat: number, lng: number }) {
    if (!this.isMobile){
      this.componentRef.instance?.zoomToMarker(coordinates.lat, coordinates.lng);
    }
  }

  ngOnInit(): void {
    this.sharedScreenSize.isMobile$.subscribe(isMobile => {
      this.isMobile = isMobile;
    });
    this.sharedOverlayService.overlayEvent$.subscribe(event => {
      this.event = event;
    });
    this.sharedOverlayService.isOverlayVisible$.subscribe(visible => {
      this.isOverlayVisible = visible;
      this.isOverlayOpening = true;
      setTimeout(() => {
        this.isOverlayOpening = false;
      }, 0);
    });
  }

  ngAfterViewInit() {
    this.loadMapUI();
  }

  async loadMapUI(){
    const { HikingMapComponent } = await import('./hiking-map/hiking-map.component');
    this.componentRef = this.container.createComponent(HikingMapComponent, {injector: this.injector})
    this.componentRef.instance.isMobile = this.isMobile;
  }

  hideOverlay(): void {
    if (this.isMobile && !this.isOverlayOpening){
      this.isOverlayVisible = false;
    }
  }

  toggleView(): void {
    this.showMap = !this.showMap;
    if (!this.showMap){
      this.isOverlayVisible = false;
    }
  }
}
