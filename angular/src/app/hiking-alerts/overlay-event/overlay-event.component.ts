import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-overlay-event',
  templateUrl: './overlay-event.component.html',
  styleUrl: './overlay-event.component.css'
})
export class OverlayEventComponent {
  @Input() event: any | null;
}
