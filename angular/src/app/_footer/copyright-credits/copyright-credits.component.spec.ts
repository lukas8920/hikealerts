import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CopyrightCreditsComponent } from './copyright-credits.component';

describe('CopyrightCreditsComponent', () => {
  let component: CopyrightCreditsComponent;
  let fixture: ComponentFixture<CopyrightCreditsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CopyrightCreditsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CopyrightCreditsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
