import { Injectable } from '@angular/core';
import {Event} from './event';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  getEvents(offset: Number, limit: Number): Observable<Event[]> {
    return new Observable<Event[]>((subscriber) => {
      subscriber.next([
        { id: "1234", title: "title1", description: "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout.", publisher: "National Park Service", create_date: "12/12/2222", lat: 51.505, lng: -0.09, url: "https://www.google.com" },
        { id: "1234", title: "title2", description: "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour,", publisher: "National Park Service", create_date: "13/13/2222", lat: 51.515, lng: -0.1, url: "https://www.google.com" },
        { id: "1234", title: "title3", description: "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old.", publisher: "National Park Service", create_date: "14/14/2222", lat: 51.525, lng: -0.08, url: "https://www.google.com" }
      ]);
      subscriber.complete();
    });
  }
}
