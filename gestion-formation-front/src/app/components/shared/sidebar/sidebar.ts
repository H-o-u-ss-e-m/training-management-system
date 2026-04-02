import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { SidebarService } from '../../../services/sidebar.service';
import { Utilisateur } from '../../../models/utilisateur.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class SidebarComponent implements OnInit, OnDestroy {

  isAdmin = false;
  isResponsable = false;
  mobileOpen = false;
  currentUser: Utilisateur | null = null;

  private subscription = new Subscription();

  constructor(
    private authService: AuthService,
    private sidebarService: SidebarService,
    private router: Router
  ) {
    console.log('SidebarComponent created');
  }

  ngOnInit(): void {
    console.log('SidebarComponent ngOnInit');
    
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.isAdmin = this.authService.isAdmin();
        this.isResponsable = this.authService.isResponsable();
        console.log('User loaded:', user.login, 'Admin:', this.isAdmin);
      }
    });

    // On écoute le service pour ouvrir/fermer le menu mobile
    this.subscription.add(
      this.sidebarService.mobileOpen$.subscribe(isOpen => {
        console.log('Sidebar received mobileOpen change:', isOpen);
        this.mobileOpen = isOpen;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  getUserInitial(): string {
    return this.currentUser?.login?.charAt(0)?.toUpperCase() || 'U';
  }

  closeMobile(): void {
    console.log('closeMobile called');
    this.sidebarService.close();
  }

  logout(): void {
    this.authService.logout();
    this.closeMobile();
    this.router.navigate(['/login']);
  }

  
}