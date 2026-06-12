import { TestBed } from '@angular/core/testing';

import { SuperAdmin } from './super-admin';

describe('SuperAdmin', () => {
  let service: SuperAdmin;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SuperAdmin);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
