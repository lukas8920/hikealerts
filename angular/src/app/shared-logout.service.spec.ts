import { TestBed } from '@angular/core/testing';

import { SharedLogoutService } from './shared-logout.service';

describe('SharedLogoutService', () => {
  let service: SharedLogoutService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedLogoutService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
