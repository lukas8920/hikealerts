import {AfterViewInit, Component, Injector, OnInit, ViewChild, ViewContainerRef} from '@angular/core';
import {SharedOverlayService} from './shared-overlay.service';
import {SharedAppService} from '../shared-app.service';
import {SharedChatService} from './shared-chat.service';

@Component({
  selector: 'app-hiking-alerts',
  templateUrl: './hiking-alerts.component.html',
  styleUrl: './hiking-alerts.component.css'
})
export class HikingAlertsComponent implements OnInit, AfterViewInit {
  @ViewChild('map_container', { read: ViewContainerRef }) map_container!: ViewContainerRef;
  @ViewChild('list_container', { read: ViewContainerRef }) list_container!: ViewContainerRef;
  @ViewChild('overlay_container', { read: ViewContainerRef }) overlay_container!: ViewContainerRef;
  @ViewChild('chat_container', { read: ViewContainerRef }) chat_container!: ViewContainerRef;

  showMap: boolean = true;
  isMobile: boolean = false;
  isOverlayVisible: boolean = false;
  isOverlayOpening: boolean = true;
  isChatVisible: boolean = false;
  isChatOpening: boolean = true;

  mapRef: any;
  listRef: any;

  constructor(private sharedOverlayService: SharedOverlayService, private sharedAppService: SharedAppService,
              private injector: Injector, private sharedChatService: SharedChatService) {
  }

  // Handle card click and pass the coordinates to Leaflet map
  onCardClick(coordinates: { lat: number, lng: number }) {
    if (!this.isMobile){
      this.mapRef.instance?.zoomToMarker(coordinates.lat, coordinates.lng);
    }
  }

  ngOnInit(): void {
    this.sharedAppService.isMobile$.subscribe(isMobile => {
      this.isMobile = isMobile;
    });
    this.sharedOverlayService.isOverlayVisible$.subscribe(visible => {
      this.isOverlayVisible = visible;
      this.isOverlayOpening = true;
      setTimeout(() => {
        this.isOverlayOpening = false;
      }, 0);
    });
    this.sharedChatService.isChatVisible$.subscribe(visible => {
      this.isChatVisible = visible;
      this.isChatOpening = true;
      setTimeout(() => {
        this.isChatOpening = false;
      }, 0);
    });
  }

  ngAfterViewInit() {
    this.loadUIElements();
  }

  async loadUIElements(){
    this.sharedAppService.updateIsNavigating(true);
    if (this.isMobile){
      // load map first
      const { HikingMapComponent } = await import('./hiking-map/hiking-map.component');
      this.mapRef = this.map_container.createComponent(HikingMapComponent, {injector: this.injector})
      this.mapRef.instance.isMobile = this.isMobile;

      // then load list
      const { AlertListComponent} = await import('./alert-list/alert-list.component');
      this.listRef = this.list_container.createComponent(AlertListComponent, {injector: this.injector});
    } else {
      // create list first
      const { AlertListComponent} = await import('./alert-list/alert-list.component');
      this.listRef = this.list_container.createComponent(AlertListComponent, {injector: this.injector});

      // then create map
      const { HikingMapComponent } = await import('./hiking-map/hiking-map.component');
      this.mapRef = this.map_container.createComponent(HikingMapComponent, {injector: this.injector})
      this.mapRef.instance.isMobile = this.isMobile;
    }
    this.sharedAppService.updateIsNavigating(false);
    //add list event emitter
    this.listRef.instance.cardClick.subscribe((data: any) => this.onCardClick(data));

    //add overlay component
    const { OverlayEventComponent } = await import('./overlay-event/overlay-event.component');
    this.overlay_container.createComponent(OverlayEventComponent, {injector: this.injector});

    const { ChatComponent } = await import('./chat/chat.component');
    this.chat_container.createComponent(ChatComponent, { injector: this.injector })
  }

  hideOverlay(): void {
    if (this.isMobile && !this.isOverlayOpening){
      this.isOverlayVisible = false;
    }
    if (!this.isChatOpening){
      this.sharedChatService.setChatVisibility(false);
    }
  }

  toggleView(): void {
    this.showMap = !this.showMap;
    if (!this.showMap){
      this.isOverlayVisible = false;
      this.isChatVisible = false;
    }
  }
}
