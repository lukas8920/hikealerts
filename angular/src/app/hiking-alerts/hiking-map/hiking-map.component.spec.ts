import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HikingMapComponent } from './hiking-map.component';

describe('HikingMapComponent', () => {
  let component: HikingMapComponent;
  let fixture: ComponentFixture<HikingMapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HikingMapComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HikingMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
