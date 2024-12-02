import {AfterViewInit, Component, DestroyRef, inject, Injector, ViewChild, ViewContainerRef} from '@angular/core';
import {TokenStorageService} from './_service/token-storage.service';
import {BreakpointObserver} from '@angular/cdk/layout';
import {SharedAppService} from './shared-app.service';
import {SharedOverlayService} from './hiking-alerts/shared-overlay.service';
import {EventType, Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements AfterViewInit {
  @ViewChild('container', { read: ViewContainerRef }) container!: ViewContainerRef;

  private destroyRef = inject(DestroyRef);

  isOpenSidebar = false;
  isSidebarOpening = false;
  isNavigating = false;

  constructor(private tokenStorageService: TokenStorageService,
              private breakpointObserver: BreakpointObserver, private sharedAppService: SharedAppService,
              private sharedOverlayService: SharedOverlayService, private injector: Injector, private router: Router) {
    this.router.events
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((e) => {
        this.navigationInterceptor(e.type);
        sharedAppService.updateIsNavigating(this.isNavigating);
      });
  }

  ngOnInit(): void {
    this.sharedAppService.isLoggedIn = !!this.tokenStorageService.getToken();
    this.sharedAppService.isNavigating$.subscribe(isNavigating => setTimeout(() => this.isNavigating = isNavigating, 0));

    if (this.sharedAppService.isLoggedIn){
      this.tokenStorageService.getUser();
    }

    this.breakpointObserver.observe(['(max-width: 768px)']).subscribe(result => {
      this.sharedAppService.updateIsMobile(result.matches);
      if (!result.matches){
        this.sharedOverlayService.setOverlayVisibility(false);
      }
    });
  }

  ngAfterViewInit(): void {
    this.loadSidebar();
  }

  private navigationInterceptor(eventType: EventType): void {
    if (eventType === EventType.NavigationStart) {
      this.isNavigating = true;
    }

    if (eventType === EventType.NavigationEnd) {
      this.isNavigating = false;
    }

    // Set loading state to false in both of the below events to hide the spinner in case a request fails
    if (eventType === EventType.NavigationCancel) {
      this.isNavigating = false;
    }

    if (eventType === EventType.NavigationError) {
      this.isNavigating = false;
    }
  }

  async loadSidebar(){
    const { SidebarComponent } = await import('./sidebar/sidebar.component');
    const sidebarRef = this.container.createComponent(SidebarComponent, {injector: this.injector});
    sidebarRef.instance.closeEvent.subscribe((data: any) => this.closeRequest());
  }

  toggleSidebar(): void {
    this.isOpenSidebar = !this.isOpenSidebar;
    if (this.isOpenSidebar){
      this.isSidebarOpening = true;
      setTimeout(() => {
        this.isSidebarOpening = false;
      }, 0);
    }
  }

  getLoginStatus(): boolean{
    return this.sharedAppService.isLoggedIn;
  }

  logout(): void{
    this.sharedAppService.logout();
  }

  closeRequest(): void {
    if (!this.isSidebarOpening){
      this.isOpenSidebar = false;
    }
  }
}
