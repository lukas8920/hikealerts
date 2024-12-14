import { TestBed } from '@angular/core/testing';

import { SharedChatService } from './shared-chat.service';

describe('SharedChatService', () => {
  let service: SharedChatService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedChatService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
