import {HTTP_INTERCEPTORS, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {TokenStorageService} from './_service/token-storage.service';
import {Observable} from 'rxjs';

const TOKEN_HEADER_KEY = 'Authorization';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private token: TokenStorageService){}

  private readonly tokenEndpoints: string[] = [
    '/v1/user/profile',
    '/v1/user/refreshApiKey',
    '/v1/user/savePassword',
    '/v1/user/deleteAccount',
    '/v1/chat/negotiate',
    '/v1/chat/communicate'
  ];

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.tokenEndpoints.some(url => req.url.includes(url))) {
      let authReq = req;
      var token = this.token.getToken();
      authReq = req.clone({
        url: `${req.url}`

      })
      if (token != null) {
        authReq = authReq.clone({
          headers: authReq.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token)
        });
      }

      return next.handle(authReq);
    }
    return next.handle(req);
  }
}

export const authInterceptorProvider = [
  {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
];
