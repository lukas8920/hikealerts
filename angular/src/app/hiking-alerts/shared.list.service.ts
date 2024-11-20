import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedListService {
  private objectListSource = new BehaviorSubject<any[]>([]);  // Initial empty list

  // Observable to allow other components to listen for updates
  eventList$ = this.objectListSource.asObservable();

  // Update the list with the future state
  updateObjectList(newList: any[]) {
    this.objectListSource.next(newList);  // Replace the current list with the new list
  }
}
