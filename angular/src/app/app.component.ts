import {AfterViewInit, Component, Injector, ViewChild, ViewContainerRef} from '@angular/core';
import {TokenStorageService} from './_service/token-storage.service';
import {SharedLogoutService} from './shared-logout.service';
import {BreakpointObserver} from '@angular/cdk/layout';
import {SharedScreenSizeService} from './shared-screen-size.service';
import {SharedOverlayService} from './hiking-alerts/shared-overlay.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements AfterViewInit {
  @ViewChild('container', { read: ViewContainerRef }) container!: ViewContainerRef;

  isOpenSidebar = false;
  isSidebarOpening = false;

  constructor(private tokenStorageService: TokenStorageService, private sharedLogoutService: SharedLogoutService,
              private breakpointObserver: BreakpointObserver, private sharedScreenSize: SharedScreenSizeService,
              private sharedOverlayService: SharedOverlayService, private injector: Injector) {
  }

  ngOnInit(): void {
    this.sharedLogoutService.isLoggedIn = !!this.tokenStorageService.getToken();

    if (this.sharedLogoutService.isLoggedIn){
      this.tokenStorageService.getUser();
    }

    this.breakpointObserver.observe(['(max-width: 768px)']).subscribe(result => {
      this.sharedScreenSize.updateIsMobile(result.matches);
      if (!result.matches){
        this.sharedOverlayService.setOverlayVisibility(false);
      }
    });
  }

  ngAfterViewInit(): void {
    this.loadSidebar();
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
    return this.sharedLogoutService.isLoggedIn;
  }

  logout(): void{
    this.sharedLogoutService.logout();
  }

  closeRequest(): void {
    if (!this.isSidebarOpening){
      this.isOpenSidebar = false;
    }
  }
}
