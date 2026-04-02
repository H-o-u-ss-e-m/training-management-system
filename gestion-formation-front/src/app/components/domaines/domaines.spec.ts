import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Domaines } from './domaines';

describe('Domaines', () => {
  let component: Domaines;
  let fixture: ComponentFixture<Domaines>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Domaines],
    }).compileComponents();

    fixture = TestBed.createComponent(Domaines);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
