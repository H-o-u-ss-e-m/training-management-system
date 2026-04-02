import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Profils } from './profils';

describe('Profils', () => {
  let component: Profils;
  let fixture: ComponentFixture<Profils>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Profils],
    }).compileComponents();

    fixture = TestBed.createComponent(Profils);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
