import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageCities } from './manage-cities';

describe('ManageCities', () => {
  let component: ManageCities;
  let fixture: ComponentFixture<ManageCities>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageCities]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageCities);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
