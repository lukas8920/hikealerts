import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverlayEventComponent } from './overlay-event.component';

describe('OverlayEventComponent', () => {
  let component: OverlayEventComponent;
  let fixture: ComponentFixture<OverlayEventComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OverlayEventComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OverlayEventComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
