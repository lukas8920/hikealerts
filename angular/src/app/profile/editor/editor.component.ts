import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {UserService} from '../../_service/user.service';
import {TokenStorageService} from '../../_service/token-storage.service';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent implements AfterViewInit, OnDestroy {
  safeUrl: SafeResourceUrl;
  isLoaded = false;
  isIframeVisible = false;

  constructor(private sanitizer: DomSanitizer, private userService: UserService,
              private storage: TokenStorageService) {
    const initialUrl = "https://hiking-alerts.org:6000"
    this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(initialUrl);
  }

  ngAfterViewInit(): void {
    this.userService.getGeotrekToken().subscribe(o => {
      const iframe = document.getElementById('geotrek-iframe') as HTMLIFrameElement;
      iframe.onload = () => {
        if(!this.isLoaded){
          this.isLoaded = true;
          iframe?.contentWindow?.postMessage({token: this.storage.getToken(), username: o.userName, password: o.password}, "*")
          this.isIframeVisible = true;
        }
      };
    })
  }

  ngOnDestroy(): void {
    this.isLoaded = false;
  }
}
