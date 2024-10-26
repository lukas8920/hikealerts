import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HikingAlertsComponent } from './hiking-alerts.component';

describe('HikingAlertsComponent', () => {
  let component: HikingAlertsComponent;
  let fixture: ComponentFixture<HikingAlertsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HikingAlertsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HikingAlertsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
