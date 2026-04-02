import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  private mobileOpenSubject = new BehaviorSubject<boolean>(false);
  public mobileOpen$ = this.mobileOpenSubject.asObservable();

  constructor() {
    console.log('SidebarService created');
  }

  open(): void {
    console.log('SidebarService: open() called');
    this.mobileOpenSubject.next(true);
    document.body.style.overflow = 'hidden';
  }

  close(): void {
    console.log('SidebarService: close() called');
    this.mobileOpenSubject.next(false);
    document.body.style.overflow = '';
  }

  toggle(): void {
    console.log('SidebarService: toggle() called, current value:', this.mobileOpenSubject.value);
    if (this.mobileOpenSubject.value) {
      this.close();
    } else {
      this.open();
    }
  }
}