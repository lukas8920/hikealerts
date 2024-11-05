import { TestBed } from '@angular/core/testing';

import { SharedProfileService } from './shared-profile.service';

describe('SharedProfileService', () => {
  let service: SharedProfileService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedProfileService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
