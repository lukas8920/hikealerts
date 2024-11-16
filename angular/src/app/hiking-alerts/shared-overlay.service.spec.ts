import { TestBed } from '@angular/core/testing';

import { SharedOverlayService } from './shared-overlay.service';

describe('SharedOverlayService', () => {
  let service: SharedOverlayService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedOverlayService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
