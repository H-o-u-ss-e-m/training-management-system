import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employeurs } from './employeurs';

describe('Employeurs', () => {
  let component: Employeurs;
  let fixture: ComponentFixture<Employeurs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Employeurs],
    }).compileComponents();

    fixture = TestBed.createComponent(Employeurs);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
