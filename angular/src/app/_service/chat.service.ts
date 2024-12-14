import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';

const headers = new HttpHeaders({'Content-Type': 'application/json'});

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  constructor(private http: HttpClient) {
  }

  initChat(): any {
    return this.http.get<any>("https://hiking-alerts.org:8080/v1/chat/init", {headers});
  }

  chat(chat: string): any {
    return this.http.post("https://hiking-alerts.org:8080/v1/chat/communicate", {'message': chat}, {headers, responseType: 'text'});
  }
}
