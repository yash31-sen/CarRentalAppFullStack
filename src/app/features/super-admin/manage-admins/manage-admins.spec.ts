import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageAdmins } from './manage-admins';

describe('ManageAdmins', () => {
  let component: ManageAdmins;
  let fixture: ComponentFixture<ManageAdmins>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageAdmins]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageAdmins);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
