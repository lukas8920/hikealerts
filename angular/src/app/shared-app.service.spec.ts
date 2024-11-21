import { TestBed } from '@angular/core/testing';

import { SharedAppService } from './shared-app.service';

describe('SharedScreenSizeService', () => {
  let service: SharedAppService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedAppService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
