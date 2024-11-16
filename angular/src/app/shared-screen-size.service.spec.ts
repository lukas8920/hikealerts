import { TestBed } from '@angular/core/testing';

import { SharedScreenSizeService } from './shared-screen-size.service';

describe('SharedScreenSizeService', () => {
  let service: SharedScreenSizeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedScreenSizeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
