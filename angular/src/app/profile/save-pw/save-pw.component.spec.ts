import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SavePwComponent } from './save-pw.component';

describe('SavePwComponent', () => {
  let component: SavePwComponent;
  let fixture: ComponentFixture<SavePwComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SavePwComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SavePwComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
