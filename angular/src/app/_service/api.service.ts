import { Injectable } from '@angular/core';
import {Event} from './event';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';

const headers = new HttpHeaders({'Content-Type': 'application/json'});

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient){}

  getEvents(offset: Number, limit: Number): Observable<Event[]> {
    let params = new HttpParams()
      .set('offset', offset.toString())
      .set('limit', limit.toString());
    return this.http.get<any>("v1/events/pull", {headers, params});
  }

  getGeoJsonLayer(): Observable<any> {
    return this.http.get<any>("v1/map/layer", {headers});
  }
}
