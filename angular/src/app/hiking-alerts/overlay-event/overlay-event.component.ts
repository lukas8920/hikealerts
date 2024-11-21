import {Component, OnInit} from '@angular/core';
import {SharedOverlayService} from '../shared-overlay.service';
import {Event} from '../../_service/event';

@Component({
  selector: 'app-overlay-event',
  templateUrl: './overlay-event.component.html',
  styleUrl: './overlay-event.component.css',
  standalone: true
})
export class OverlayEventComponent implements OnInit{
  event: Event | null = null;

  constructor(private sharedOverlayService: SharedOverlayService) {
  }

  ngOnInit(): void {
    this.sharedOverlayService.overlayEvent$.subscribe(e => {
      this.event = e;
    });
  }
}
