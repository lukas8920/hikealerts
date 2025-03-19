import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {UserService} from '../../_service/user.service';
import {TokenStorageService} from '../../_service/token-storage.service';
import {SharedAppService} from '../../shared-app.service';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrl: './editor.component.css'
})
export class EditorComponent implements AfterViewInit, OnDestroy {
  initialUrl = "https://hiking-alerts.org:4200";
  pathUrl = "http://hiking-alerts.org:4200/path/list/";

  safeUrl: SafeResourceUrl;
  isLoaded = false;
  isIframeVisible = false;
  isLoading = false;

  constructor(private sanitizer: DomSanitizer, private userService: UserService,
              private storage: TokenStorageService, private sharedAppService: SharedAppService) {
    this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.initialUrl);
  }

  private messageListener = (event: any) => {
    if (event.origin !== "https://hiking-alerts.org") return;
    const {type} = event.data

    if (type == "LOGIN"){
      this.requestLogin();
    } else if (type == "LOADED"){
      console.log("log in completed");
      this.isLoading = false;
      this.isIframeVisible = true;
      this.sharedAppService.updateIsNavigating(false);
    } else if (type == "RUNNING"){
      console.log("active session is running");
      this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.pathUrl);
      this.isLoading = false;
      this.isIframeVisible = true;
    }
  };

  requestLogin(): void {
    const iframe = document.getElementById('geotrek-iframe') as HTMLIFrameElement;
    if (!this.isIframeVisible){
      this.sharedAppService.updateIsNavigating(true);
    }
    this.userService.getGeotrekToken().subscribe(o => {
      const iframe = document.getElementById('geotrek-iframe') as HTMLIFrameElement;
      iframe.onload = () => {
        iframe?.contentWindow?.postMessage({type: "LOGIN", token: this.storage.getToken(), username: o.userName, password: o.password}, "*")
      };
    })
  }

  ngAfterViewInit(): void {
    window.addEventListener('message', this.messageListener);
    const iframe = document.getElementById('geotrek-iframe') as HTMLIFrameElement;
    iframe.onload = () => {
      console.log("send init request");
      if (!this.isIframeVisible){
        this.isLoading = true;
        iframe?.contentWindow?.postMessage({type: "INIT"}, "*");
      }
    }
  }

  ngOnDestroy(): void {
    this.isIframeVisible = false;
    this.isLoading = false;
    window.removeEventListener('message', this.messageListener);
  }
}
