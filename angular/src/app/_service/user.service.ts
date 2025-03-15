import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'}),
};

const textOptions = {
  headers: new HttpHeaders({'Content-Type': 'text/plain'})
};

@Injectable({
  providedIn: 'root'
})
export class UserService {
  test_url: string = "http://localhost:8080";
  prod_url: string = "https://hiking-alerts.org:8080"

  constructor(private http: HttpClient) { }

  register(mail: string, password: string): Observable<any> {
    return this.http.post(this.prod_url + '/v1/auth/register', {
      mail, password
    }, httpOptions);
  }

  login(mail: string, password: string): Observable<any>{
    return this.http.post(this.prod_url + '/v1/auth/login', {
      mail, password}, httpOptions);
  }

  resetPw(mail: string): Observable<any>{
    return this.http.post(this.prod_url + '/v1/user/resetPassword', mail, textOptions);
  }

  changePw(newPassword: string, token: string | null): Observable<any> {
    return this.http.post(this.prod_url + '/v1/user/changePassword', {
      newPassword, token
    }, httpOptions);
  }

  savePw(newPassword: string, token: string): Observable<any> {
    return this.http.post( this.prod_url + '/v1/user/savePassword', {
      newPassword, token}, httpOptions);
  }

  refreshApiKey(): Observable<any> {
    return this.http.post(this.prod_url + '/v1/user/refreshApiKey', httpOptions);
  }

  deleteAccount(): Observable<any>{
    return this.http.post( this.prod_url +'/v1/user/deleteAccount', httpOptions);
  }

  getProfile(): Observable<any>{
    return this.http.get(this.prod_url + '/v1/user/profile', httpOptions);
  }

  getGeotrekToken(): Observable<any> {
    return this.http.get(this.prod_url + '/v1/geotrek/credentials', httpOptions);
  }
}
