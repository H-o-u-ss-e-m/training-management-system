import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { StatistiqueService } from '../../services/statistique.service';
import { Totaux } from '../../models/statistique.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent, SidebarComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  totaux: Totaux = {
    formations: 0,
    participants: 0,
    formateurs: 0
  };

  isLoading = true;
  today: Date = new Date();
  private subscription: Subscription | null = null;

  constructor(
    private statistiqueService: StatistiqueService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('Dashboard initialisé - Début chargement');
    this.loadTotaux();
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  loadTotaux(): void {
    console.log('Chargement des totaux...');
    this.isLoading = true;
    this.cdr.detectChanges();
    
    this.subscription = this.statistiqueService.getTotaux().subscribe({
      next: (data) => {
        console.log('Données reçues:', data);
        this.totaux = {
          formations: data.formations || 0,
          participants: data.participants || 0,
          formateurs: data.formateurs || 0
        };
        this.isLoading = false;
        this.cdr.detectChanges();
        console.log('isLoading après réception:', this.isLoading);
        console.log('totaux:', this.totaux);
      },
      error: (error) => {
        console.error('Erreur chargement totaux:', error);
        this.isLoading = false;
        this.cdr.detectChanges();
        // Données de démonstration si erreur
        this.totaux = { formations: 6, participants: 9, formateurs: 8 };
      }
    });
  }

  navigateTo(route: string): void {
    console.log('Navigation vers:', route);
    this.router.navigate([route]).then(success => {
      if (success) {
        console.log('Navigation réussie vers', route);
      } else {
        console.error('Échec de navigation vers', route);
      }
    });
  }
}