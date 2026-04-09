import { Component, OnInit, OnDestroy, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StatistiqueService } from '../../services/statistique.service';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';
import { StatDTO, Totaux } from '../../models/statistique.model';
import Chart from 'chart.js/auto';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-statistiques',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, SidebarComponent],
  templateUrl: './statistiques.html',
  styleUrls: ['./statistiques.css']
})
export class StatistiquesComponent implements OnInit, OnDestroy, AfterViewInit {

  isLoading = true;
  hasError = false;
  errorMessage = '';
  today: Date = new Date();

  totaux: Totaux = { formations: 0, participants: 0, formateurs: 0 };
  formationsParAnnee: StatDTO[] = [];
  formationsParDomaine: StatDTO[] = [];
  formationsParStructure: StatDTO[] = [];
  formationsParMois: StatDTO[] = [];
  tauxParticipation: number = 0;

  private chartAnnee: Chart | null = null;
  private chartDomaine: Chart | null = null;
  private chartStructure: Chart | null = null;
  private chartEvolution: Chart | null = null;

  private isDestroyed = false;
  private chartTimeout: any = null;
  private isFirstLoad = true;

  evolutionData = {
    labels: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'],
    formations: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    participants: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
  };

  constructor(
    private statistiqueService: StatistiqueService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isDestroyed = false;
    this.isFirstLoad = true;
    this.loadAllData();
  }

  ngAfterViewInit(): void {
    if (!this.isLoading && !this.hasError && this.totaux.formations > 0) {
      setTimeout(() => {
        if (!this.isDestroyed) {
          this.destroyCharts();
          this.createAllCharts();
        }
      }, 100);
    }
  }

  ngOnDestroy(): void {
    this.isDestroyed = true;
    
    if (this.chartTimeout) {
      clearTimeout(this.chartTimeout);
      this.chartTimeout = null;
    }
    
    this.destroyCharts();
  }

  /**
   * Charge toutes les données du backend
   */
  async loadAllData(): Promise<void> {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';

    try {
      const [totaux, parAnnee, parDomaine, parStructure, parMois] = await Promise.all([
        firstValueFrom(this.statistiqueService.getTotaux()),
        firstValueFrom(this.statistiqueService.formationsParAnnee()),
        firstValueFrom(this.statistiqueService.formationsParDomaine()),
        firstValueFrom(this.statistiqueService.formationsParStructure()),
        firstValueFrom(this.statistiqueService.formationsParMois())
      ]);

      if (this.isDestroyed) {
        return;
      }

      this.totaux = totaux ?? { formations: 0, participants: 0, formateurs: 0 };
      this.formationsParAnnee = parAnnee ?? [];
      this.formationsParDomaine = parDomaine ?? [];
      this.formationsParStructure = parStructure ?? [];
      this.formationsParMois = parMois ?? [];

      this.tauxParticipation = this.calcTaux();
      this.generateEvolutionData();

      this.isLoading = false;
      this.isFirstLoad = false;

      if (!this.isDestroyed) {
        this.cdr.detectChanges();
      }

      if (this.chartTimeout) {
        clearTimeout(this.chartTimeout);
      }

      this.chartTimeout = setTimeout(() => {
        if (!this.isDestroyed) {
          this.createAllCharts();
        }
      }, 200);

    } catch (err: any) {
      if (this.isDestroyed) {
        return;
      }

      this.isLoading = false;
      this.hasError = true;

      if (err.status === 0) {
        this.errorMessage = '❌ Serveur indisponible. Assurez-vous que le backend est en cours d\'exécution sur http://localhost:8080';
      } else if (err.status === 500) {
        this.errorMessage = '❌ Erreur serveur 500. Vérifiez les logs du backend pour plus de détails.';
      } else if (err.status === 404) {
        this.errorMessage = '❌ Endpoint non trouvé (404). Vérifiez les URL de l\'API.';
      } else {
        this.errorMessage = `❌ Erreur ${err.status}: ${err.message || 'Impossible de charger les données'}`;
      }

      try {
        this.cdr.detectChanges();
      } catch (cdrErr) {
        // Ignorer l'erreur de détection
      }
    }
  }

  /**
   * Calcule le taux de participation
   */
  private calcTaux(): number {
    if (this.totaux.formations === 0) return 0;
    const moyenne = this.totaux.participants / this.totaux.formations;
    return Math.min(Math.round((moyenne / 15) * 100), 100);
  }

  /**
   * Génère les données d'évolution mensuelle
   */
  generateEvolutionData(): void {
    const totalFormations = this.totaux.formations;
    const totalParticipants = this.totaux.participants;

    if (totalFormations > 0 && this.formationsParMois.length > 0) {
      const moisMap = new Map<number, number>();
      this.formationsParMois.forEach(d => {
        const moisNum = parseInt(d.label.split('-')[0], 10);
        moisMap.set(moisNum, (moisMap.get(moisNum) || 0) + d.count);
      });

      for (let i = 0; i < 12; i++) {
        this.evolutionData.formations[i] = moisMap.get(i + 1) || 0;
        this.evolutionData.participants[i] = Math.round(
          (moisMap.get(i + 1) || 0) * (totalParticipants / Math.max(totalFormations, 1))
        );
      }
    } else if (totalFormations > 0) {
      const coeff = [0.05, 0.06, 0.07, 0.08, 0.09, 0.12, 0.1, 0.07, 0.08, 0.1, 0.1, 0.08];
      for (let i = 0; i < 12; i++) {
        this.evolutionData.formations[i] = Math.round(totalFormations * coeff[i]);
        this.evolutionData.participants[i] = Math.round(totalParticipants * coeff[i]);
      }
    } else {
      this.evolutionData.formations = [2, 3, 4, 3, 5, 7, 6, 4, 5, 6, 7, 5];
      this.evolutionData.participants = [20, 25, 30, 28, 35, 45, 40, 32, 38, 42, 48, 40];
    }
  }

  /**
   * Recharge les données
   */
  refreshData(): void {
    this.destroyCharts();
    this.loadAllData();
  }

  // ─────────────────────── CHARTS ───────────────────────

  private createAllCharts(): void {
    const evolutionCanvas = document.getElementById('chartEvolution');
    const anneeCanvas = document.getElementById('chartAnnee');
    const domaineCanvas = document.getElementById('chartDomaine');
    const structureCanvas = document.getElementById('chartStructure');
    
    if (!evolutionCanvas && !anneeCanvas && !domaineCanvas && !structureCanvas) {
      setTimeout(() => {
        if (!this.isDestroyed) {
          this.createAllCharts();
        }
      }, 100);
      return;
    }
    
    this.createEvolutionChart();
    this.createAnneeChart();
    this.createDomaineChart();
    this.createStructureChart();
  }

  private destroyCharts(): void {
    if (this.chartEvolution) {
      this.chartEvolution.destroy();
      this.chartEvolution = null;
    }
    if (this.chartAnnee) {
      this.chartAnnee.destroy();
      this.chartAnnee = null;
    }
    if (this.chartDomaine) {
      this.chartDomaine.destroy();
      this.chartDomaine = null;
    }
    if (this.chartStructure) {
      this.chartStructure.destroy();
      this.chartStructure = null;
    }
  }

  private createEvolutionChart(): void {
    const canvas = document.getElementById('chartEvolution') as HTMLCanvasElement;
    if (!canvas) return;
    
    if (this.chartEvolution) {
      this.chartEvolution.destroy();
      this.chartEvolution = null;
    }

    this.chartEvolution = new Chart(canvas, {
      type: 'line',
      data: {
        labels: this.evolutionData.labels,
        datasets: [
          {
            label: 'Formations',
            data: this.evolutionData.formations,
            borderColor: '#667eea',
            backgroundColor: 'rgba(102, 126, 234, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            pointBackgroundColor: '#667eea',
            pointBorderColor: 'white',
            pointBorderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6
          },
          {
            label: 'Participants',
            data: this.evolutionData.participants,
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245, 158, 11, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            pointBackgroundColor: '#f59e0b',
            pointBorderColor: 'white',
            pointBorderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top' },
          tooltip: { mode: 'index', intersect: false }
        },
        scales: {
          y: { beginAtZero: true, grid: { color: '#e5e7eb' } },
          x: { grid: { display: false } }
        }
      }
    });
  }

  private createAnneeChart(): void {
    const canvas = document.getElementById('chartAnnee') as HTMLCanvasElement;
    if (!canvas || this.formationsParAnnee.length === 0) return;
    
    if (this.chartAnnee) {
      this.chartAnnee.destroy();
      this.chartAnnee = null;
    }

    const sorted = [...this.formationsParAnnee].sort((a, b) => parseInt(a.label) - parseInt(b.label));

    this.chartAnnee = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: sorted.map(d => d.label),
        datasets: [{
          label: 'Formations',
          data: sorted.map(d => d.count),
          backgroundColor: 'rgba(102, 126, 234, 0.7)',
          borderColor: '#667eea',
          borderWidth: 2,
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: (ctx: any) => `${ctx.raw} formation(s)` } }
        },
        scales: {
          y: { beginAtZero: true },
          x: { grid: { display: false } }
        }
      }
    });
  }

  private createDomaineChart(): void {
    const canvas = document.getElementById('chartDomaine') as HTMLCanvasElement;
    if (!canvas || this.formationsParDomaine.length === 0) return;
    
    if (this.chartDomaine) {
      this.chartDomaine.destroy();
      this.chartDomaine = null;
    }

    const colors = ['#667eea', '#764ba2', '#f59e0b', '#10b981', '#ef4444', '#3b82f6', '#ec489a', '#8b5cf6'];

    this.chartDomaine = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: this.formationsParDomaine.map(d => d.label),
        datasets: [{
          data: this.formationsParDomaine.map(d => d.count),
          backgroundColor: colors.slice(0, this.formationsParDomaine.length),
          borderWidth: 0,
          hoverOffset: 10
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { font: { size: 11 }, usePointStyle: true } },
          tooltip: {
            callbacks: {
              label: (ctx: any) => {
                const total = ctx.dataset.data.reduce((a: number, b: number) => a + b, 0);
                return `${ctx.label}: ${ctx.raw} (${((ctx.raw / total) * 100).toFixed(1)}%)`;
              }
            }
          }
        },
        cutout: '60%'
      }
    });
  }

  private createStructureChart(): void {
    const canvas = document.getElementById('chartStructure') as HTMLCanvasElement;
    if (!canvas || this.formationsParStructure.length === 0) return;
    
    if (this.chartStructure) {
      this.chartStructure.destroy();
      this.chartStructure = null;
    }

    const maxVal = Math.max(...this.formationsParStructure.map(d => d.count), 10);

    this.chartStructure = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.formationsParStructure.map(d => d.label),
        datasets: [{
          label: 'Participants',
          data: this.formationsParStructure.map(d => d.count),
          backgroundColor: 'rgba(16, 185, 129, 0.7)',
          borderColor: '#10b981',
          borderWidth: 2,
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: (ctx: any) => `${ctx.raw} participant(s)` } }
        },
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: Math.ceil(maxVal / 5) } },
          x: { grid: { display: false } }
        }
      }
    });
  }

  // ─────────────────────── HELPERS ───────────────────────

  getParticipationRate(): number {
    return this.tauxParticipation;
  }

  getTopDomaine(): string {
    if (this.formationsParDomaine.length === 0) return 'Aucune donnée';
    return [...this.formationsParDomaine].sort((a, b) => b.count - a.count)[0].label;
  }

  getTopMois(): string {
    const mois = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
    const maxIdx = this.evolutionData.formations.indexOf(Math.max(...this.evolutionData.formations));
    return mois[maxIdx] || 'N/A';
  }
}