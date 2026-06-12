import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagePricing } from './manage-pricing';

describe('ManagePricing', () => {
  let component: ManagePricing;
  let fixture: ComponentFixture<ManagePricing>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManagePricing]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManagePricing);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
