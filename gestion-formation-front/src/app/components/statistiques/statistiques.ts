import { Component, OnInit, OnDestroy, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StatistiqueService } from '../../services/statistique.service';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';
import { StatDTO, Totaux } from '../../models/statistique.model';
import Chart from 'chart.js/auto';
import { firstValueFrom } from 'rxjs';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

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

      if (this.isDestroyed) return;

      this.totaux = totaux ?? { formations: 0, participants: 0, formateurs: 0 };
      this.formationsParAnnee = parAnnee ?? [];
      this.formationsParDomaine = parDomaine ?? [];
      this.formationsParStructure = parStructure ?? [];
      this.formationsParMois = parMois ?? [];

      this.tauxParticipation = this.calcTaux();
      this.generateEvolutionData();

      this.isLoading = false;
      this.isFirstLoad = false;
      this.cdr.detectChanges();

      this.chartTimeout = setTimeout(() => {
        if (!this.isDestroyed) this.createAllCharts();
      }, 200);

    } catch (err: any) {
      if (this.isDestroyed) return;
      this.isLoading = false;
      this.hasError = true;
      this.errorMessage = err.status === 0 ? 'Serveur indisponible' : `Erreur ${err.status}`;
      this.cdr.detectChanges();
    }
  }

  private calcTaux(): number {
    if (this.totaux.formations === 0) return 0;
    const moyenne = this.totaux.participants / this.totaux.formations;
    return Math.min(Math.round((moyenne / 15) * 100), 100);
  }

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

  refreshData(): void {
    this.destroyCharts();
    this.loadAllData();
  }

  // ==================== EXPORT PDF ====================

  async exportToPDF(): Promise<void> {
    if (this.isLoading || this.hasError) {
      alert("Veuillez attendre le chargement complet des donnees.");
      return;
    }

    const loadingDiv = this.showLoadingIndicator();

    try {
      await this.delay(500);

      const doc = new jsPDF({
        orientation: 'portrait',
        unit: 'mm',
        format: 'a4'
      });

      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();

      this.addCoverPage(doc, pageWidth, pageHeight);
      this.addExecutiveSummary(doc, pageWidth, pageHeight);
      await this.addKPIPageV2(doc, pageWidth, pageHeight);
      await this.addEvolutionPageV2(doc, pageWidth, pageHeight);
      await this.addAnneeDomainePageV2(doc, pageWidth, pageHeight);
      await this.addStructureInsightsPageV2(doc, pageWidth, pageHeight);

      const fileName = `Rapport_Statistiques_ExcellentTraining_${this.today.toISOString().slice(0, 10)}.pdf`;
      doc.save(fileName);

    } catch (error) {
      console.error('PDF Error:', error);
      alert("Erreur lors de la generation du PDF.");
    } finally {
      document.body.removeChild(loadingDiv);
    }
  }

  /**
   * Capture un graphique Chart.js en haute resolution (4x) pour un rendu net dans le PDF.
   * Force Chart.js a re-rendre a grande taille, capture, puis restaure l'original.
   */
  private async captureChart(chartId: string): Promise<string | null> {
    const originalCanvas = document.getElementById(chartId) as HTMLCanvasElement;
    if (!originalCanvas) return null;

    const chartInstance = Chart.getChart(originalCanvas);

    // Fallback si pas d'instance Chart.js trouvee
    if (!chartInstance) {
      const fallback = document.createElement('canvas');
      fallback.width = originalCanvas.width * 3;
      fallback.height = originalCanvas.height * 3;
      const fc = fallback.getContext('2d')!;
      fc.fillStyle = '#ffffff';
      fc.fillRect(0, 0, fallback.width, fallback.height);
      fc.scale(3, 3);
      fc.drawImage(originalCanvas, 0, 0);
      return fallback.toDataURL('image/png', 1.0);
    }

    const SCALE = 4;

    // Sauvegarder les dimensions originales
    const origWidth  = originalCanvas.width;
    const origHeight = originalCanvas.height;
    const origStyleWidth  = originalCanvas.style.width;
    const origStyleHeight = originalCanvas.style.height;

    try {
      // Agrandir physiquement le canvas a 4x
      originalCanvas.width  = origWidth  * SCALE;
      originalCanvas.height = origHeight * SCALE;

      // Forcer Chart.js a tout redessiner a la nouvelle taille
      chartInstance.resize();
      chartInstance.render();

      // Attendre que le rendu soit complet
      await this.delay(120);

      // Creer un canvas temporaire avec fond blanc et copier le rendu
      const tempCanvas = document.createElement('canvas');
      tempCanvas.width  = originalCanvas.width;
      tempCanvas.height = originalCanvas.height;
      const ctx = tempCanvas.getContext('2d')!;
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, tempCanvas.width, tempCanvas.height);
      ctx.drawImage(originalCanvas, 0, 0);

      return tempCanvas.toDataURL('image/png', 1.0);

    } finally {
      // Toujours restaurer les dimensions originales
      originalCanvas.width  = origWidth;
      originalCanvas.height = origHeight;
      originalCanvas.style.width  = origStyleWidth;
      originalCanvas.style.height = origStyleHeight;
      chartInstance.resize();
      chartInstance.render();
      await this.delay(120);
    }
  }

  private showLoadingIndicator(): HTMLDivElement {
    const div = document.createElement('div');
    div.innerHTML = `
      <div style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);
                  background:#1e293b;color:white;padding:20px 40px;border-radius:12px;
                  z-index:10000;text-align:center;box-shadow:0 10px 40px rgba(0,0,0,0.3);">
        <div style="width:40px;height:40px;border:3px solid #f59e0b;border-top-color:white;
                    border-radius:50%;animation:spin 1s linear infinite;margin:0 auto 15px;"></div>
        <p style="margin:0;font-size:14px;">Generation du rapport PDF...</p>
        <p style="margin:5px 0 0;font-size:11px;color:#94a3b8;">Veuillez patienter</p>
      </div>
      <style>@keyframes spin{to{transform:rotate(360deg)}}</style>
    `;
    document.body.appendChild(div);
    return div;
  }

  private drawBullet(doc: jsPDF, x: number, y: number, color: number[]): void {
    doc.setFillColor(color[0], color[1], color[2]);
    doc.circle(x, y - 1.5, 1.5, 'F');
  }

  private addCoverPage(doc: jsPDF, pageWidth: number, pageHeight: number): void {
    const cx = pageWidth / 2;

    // Bandes superieures
    doc.setFillColor(102, 126, 234);
    doc.rect(0, 0, pageWidth, 8, 'F');
    doc.setFillColor(139, 92, 246);
    doc.rect(0, 8, pageWidth, 4, 'F');

    // Bloc logo decoratif
    doc.setFillColor(102, 126, 234);
    doc.roundedRect(cx - 28, 32, 56, 56, 6, 6, 'F');
    doc.setFillColor(255, 255, 255);
    doc.rect(cx - 18, 44, 36, 5, 'F');
    doc.rect(cx - 18, 53, 36, 5, 'F');
    doc.rect(cx - 18, 62, 24, 5, 'F');

    // Texte EXCELLENT TRAINING
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(32);
    doc.setTextColor(102, 126, 234);
    doc.text("EXCELLENT", cx, 110, { align: 'center' });
    doc.setFontSize(24);
    doc.setTextColor(139, 92, 246);
    doc.text("TRAINING", cx, 126, { align: 'center' });

    // Separateur
    doc.setDrawColor(203, 213, 225);
    doc.setLineWidth(0.5);
    doc.line(50, 138, pageWidth - 50, 138);

    // Titres rapport
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(22);
    doc.setTextColor(30, 41, 59);
    doc.text("RAPPORT STATISTIQUE", cx, 162, { align: 'center' });
    doc.setFontSize(18);
    doc.setTextColor(71, 85, 105);
    doc.text("ANALYTIQUE", cx, 178, { align: 'center' });

    // Badge annee
    doc.setFillColor(238, 242, 255);
    doc.roundedRect(cx - 30, 188, 60, 14, 7, 7, 'F');
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    doc.setTextColor(102, 126, 234);
    doc.text(`Annee ${this.today.getFullYear()}`, cx, 197, { align: 'center' });

    // Ligne decorative centrale
    doc.setFillColor(102, 126, 234);
    doc.rect(cx - 1, 210, 2, 30, 'F');
    doc.setFillColor(139, 92, 246);
    doc.circle(cx, 245, 3, 'F');

    // Date edition
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(148, 163, 184);
    const dateStr = this.today.toLocaleDateString('fr-FR', {
      day: 'numeric', month: 'long', year: 'numeric'
    });
    doc.text(`Etabli le ${dateStr}`, cx, 268, { align: 'center' });

    // Blocs stats en bas
    const stats = [
      { label: 'Formations',  value: this.totaux.formations.toString(),  color: [102, 126, 234] as number[] },
      { label: 'Participants', value: this.totaux.participants.toString(), color: [16, 185, 129]  as number[] },
      { label: 'Formateurs',  value: this.totaux.formateurs.toString(),  color: [139, 92, 246]  as number[] }
    ];
    const blockW = 44;
    const blockSpacing = 16;
    const totalW = stats.length * blockW + (stats.length - 1) * blockSpacing;
    const startX = cx - totalW / 2;

    stats.forEach((s, i) => {
      const bx = startX + i * (blockW + blockSpacing);
      const by = pageHeight - 60;
      doc.setFillColor(s.color[0], s.color[1], s.color[2]);
      doc.roundedRect(bx, by, blockW, 28, 4, 4, 'F');
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(16);
      doc.setTextColor(255, 255, 255);
      doc.text(s.value, bx + blockW / 2, by + 12, { align: 'center' });
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(7);
      doc.text(s.label.toUpperCase(), bx + blockW / 2, by + 22, { align: 'center' });
    });

    // Bande inferieure
    doc.setFillColor(102, 126, 234);
    doc.rect(0, pageHeight - 8, pageWidth, 8, 'F');

    this.addFooter(doc, 1, pageWidth, pageHeight);
  }

  private addExecutiveSummary(doc: jsPDF, pageWidth: number, pageHeight: number): void {
    doc.addPage();

    doc.setFillColor(102, 126, 234);
    doc.rect(0, 0, pageWidth, 12, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(16);
    doc.setTextColor(255, 255, 255);
    doc.text("RESUME EXECUTIF", 20, 9);

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(13);
    doc.setTextColor(30, 41, 59);
    doc.text("Synthese des performances", 20, 32);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(71, 85, 105);

    const bulletColor = [102, 126, 234];
    const items = [
      `${this.totaux.formations} formations ont ete organisees, demontrant une activite soutenue.`,
      `${this.totaux.participants} participants ont suivi nos formations, temoignant de notre impact.`,
      `${this.totaux.formateurs} formateurs experts contribuent a la qualite de nos programmes.`,
      `Taux de participation global de ${this.tauxParticipation}%, refletant l'engagement des apprenants.`,
      `Le domaine "${this.getTopDomaine()}" est le plus demande par nos clients.`,
      `Le mois de ${this.getTopMois()} a enregistre le pic d'activite de l'annee.`
    ];

    let y = 50;
    items.forEach(line => {
      this.drawBullet(doc, 25, y, bulletColor);
      doc.text(line, 31, y);
      y += 9;
    });

    // Encadre objectif
    y += 8;
    doc.setFillColor(248, 250, 252);
    doc.roundedRect(20, y, pageWidth - 40, 38, 5, 5, 'F');
    doc.setDrawColor(102, 126, 234);
    doc.setLineWidth(0.5);
    doc.roundedRect(20, y, pageWidth - 40, 38, 5, 5, 'D');
    doc.setFillColor(102, 126, 234);
    doc.roundedRect(20, y, 4, 38, 2, 2, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(10);
    doc.setTextColor(102, 126, 234);
    doc.text("OBJECTIF STRATEGIQUE", 30, y + 10);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(71, 85, 105);
    doc.text("Atteindre 100 formations et 500 participants d'ici la fin de l'exercice.", 30, y + 22);
    doc.text("Developper de nouveaux domaines a forte valeur ajoutee.", 30, y + 32);

    // Bande progression
    y += 55;
    doc.setFillColor(16, 185, 129);
    doc.roundedRect(20, y, pageWidth - 40, 18, 4, 4, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(10);
    doc.setTextColor(255, 255, 255);
    const progress = Math.min(Math.round((this.totaux.formations / 100) * 100), 100);
    doc.text(
      `Progression vers objectif: ${progress}% des formations cible atteintes`,
      pageWidth / 2, y + 11, { align: 'center' }
    );

    this.addFooter(doc, doc.getCurrentPageInfo().pageNumber, pageWidth, pageHeight);
  }

  private async addKPIPageV2(doc: jsPDF, pageWidth: number, pageHeight: number): Promise<void> {
    doc.addPage();

    doc.setFillColor(102, 126, 234);
    doc.rect(0, 0, pageWidth, 12, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(16);
    doc.setTextColor(255, 255, 255);
    doc.text("INDICATEURS CLES DE PERFORMANCE", 20, 9);

    const kpis = [
      { label: "Formations",        value: this.totaux.formations.toString(),  abbr: "FORM", color: [102, 126, 234] as number[], bgColor: [238, 242, 255] as number[] },
      { label: "Participants",       value: this.totaux.participants.toString(), abbr: "PART", color: [16, 185, 129]  as number[], bgColor: [236, 253, 245] as number[] },
      { label: "Formateurs",         value: this.totaux.formateurs.toString(),  abbr: "FORM", color: [139, 92, 246]  as number[], bgColor: [245, 243, 255] as number[] },
      { label: "Taux participation", value: `${this.tauxParticipation}%`,       abbr: "TAUX", color: [245, 158, 11]  as number[], bgColor: [255, 251, 235] as number[] }
    ];

    const cardW = (pageWidth - 50) / 2;
    const cardH = 50;
    const startY = 28;

    kpis.forEach((kpi, idx) => {
      const col = idx % 2;
      const row = Math.floor(idx / 2);
      const x = 20 + col * (cardW + 10);
      const y = startY + row * (cardH + 10);

      // Fond carte
      doc.setFillColor(kpi.bgColor[0], kpi.bgColor[1], kpi.bgColor[2]);
      doc.roundedRect(x, y, cardW, cardH, 6, 6, 'F');
      doc.setDrawColor(220, 225, 240);
      doc.setLineWidth(0.3);
      doc.roundedRect(x, y, cardW, cardH, 6, 6, 'D');

      // Barre laterale coloree
      doc.setFillColor(kpi.color[0], kpi.color[1], kpi.color[2]);
      doc.roundedRect(x, y, 5, cardH, 3, 3, 'F');

      // Carre icone
      doc.setFillColor(kpi.color[0], kpi.color[1], kpi.color[2]);
      doc.roundedRect(x + 13, y + 10, 30, 30, 4, 4, 'F');
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(8);
      doc.setTextColor(255, 255, 255);
      doc.text(kpi.abbr, x + 28, y + 28, { align: 'center' });

      // Label
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(9);
      doc.setTextColor(100, 116, 139);
      doc.text(kpi.label.toUpperCase(), x + 52, y + 18);

      // Valeur
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(26);
      doc.setTextColor(kpi.color[0], kpi.color[1], kpi.color[2]);
      doc.text(kpi.value, x + 52, y + 40);
    });

    // Note source
    const noteY = startY + 2 * (cardH + 10) + 12;
    doc.setFillColor(248, 250, 252);
    doc.roundedRect(20, noteY, pageWidth - 40, 22, 4, 4, 'F');
    doc.setFont('helvetica', 'italic');
    doc.setFontSize(8);
    doc.setTextColor(148, 163, 184);
    doc.text(
      "Source: Systeme de gestion Excellent Training - Donnees consolidees",
      pageWidth / 2, noteY + 13, { align: 'center' }
    );

    this.addFooter(doc, doc.getCurrentPageInfo().pageNumber, pageWidth, pageHeight);
  }

  private async addEvolutionPageV2(doc: jsPDF, pageWidth: number, pageHeight: number): Promise<void> {
    doc.addPage();

    doc.setFillColor(102, 126, 234);
    doc.rect(0, 0, pageWidth, 12, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(16);
    doc.setTextColor(255, 255, 255);
    doc.text("EVOLUTION MENSUELLE", 20, 9);

    // ✅ Capture haute resolution
    const imgEvolution = await this.captureChart('chartEvolution');
    if (imgEvolution) {
      doc.addImage(imgEvolution, 'PNG', 20, 20, pageWidth - 40, 95);
    }

    // Legende manuelle
    const legendY = 120;
    doc.setFillColor(102, 126, 234);
    doc.circle(32, legendY, 3, 'F');
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(71, 85, 105);
    doc.text("Formations", 38, legendY + 1.5);

    doc.setFillColor(245, 158, 11);
    doc.circle(85, legendY, 3, 'F');
    doc.text("Participants", 91, legendY + 1.5);

    // Encadre analyse tendance
    const maxFormations  = Math.max(...this.evolutionData.formations);
    const maxMonthLabel  = this.evolutionData.labels[this.evolutionData.formations.indexOf(maxFormations)];
    const totalFormations = this.evolutionData.formations.reduce((a, b) => a + b, 0);

    const boxY = 130;
    doc.setFillColor(248, 250, 252);
    doc.roundedRect(20, boxY, pageWidth - 40, 52, 5, 5, 'F');
    doc.setDrawColor(226, 232, 240);
    doc.setLineWidth(0.3);
    doc.roundedRect(20, boxY, pageWidth - 40, 52, 5, 5, 'D');
    doc.setFillColor(102, 126, 234);
    doc.roundedRect(20, boxY, 4, 52, 2, 2, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(11);
    doc.setTextColor(30, 41, 59);
    doc.text("ANALYSE DE TENDANCE", 30, boxY + 12);

    doc.setDrawColor(226, 232, 240);
    doc.line(30, boxY + 17, pageWidth - 25, boxY + 17);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(71, 85, 105);

    const trendLines = [
      `Point culminant: ${maxMonthLabel} (${maxFormations} formations) - Periode de forte activite`,
      `Moyenne mensuelle: ${Math.round(totalFormations / 12)} formations`,
      `Variation: Activite ${maxFormations > 10 ? 'en croissance' : 'stable'} sur l'annee`
    ];

    trendLines.forEach((line, i) => {
      this.drawBullet(doc, 34, boxY + 26 + i * 10, [102, 126, 234]);
      doc.text(line, 40, boxY + 26 + i * 10);
    });

    this.addFooter(doc, doc.getCurrentPageInfo().pageNumber, pageWidth, pageHeight);
  }

  private async addAnneeDomainePageV2(doc: jsPDF, pageWidth: number, pageHeight: number): Promise<void> {
    doc.addPage();

    doc.setFillColor(102, 126, 234);
    doc.rect(0, 0, pageWidth, 12, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(16);
    doc.setTextColor(255, 255, 255);
    doc.text("ANALYSE PAR ANNEE ET PAR DOMAINE", 20, 9);

    const halfW    = (pageWidth - 50) / 2;
    const xDomaine = pageWidth - 20 - halfW;

    // ✅ Capture haute resolution graphique Annee
    const imgAnnee = await this.captureChart('chartAnnee');
    if (imgAnnee) {
      doc.addImage(imgAnnee, 'PNG', 20, 20, halfW, 80);

      doc.setFont('helvetica', 'bold');
      doc.setFontSize(10);
      doc.setTextColor(71, 85, 105);
      doc.text("Formations par annee", 20 + halfW / 2, 108, { align: 'center' });

      if (this.formationsParAnnee.length > 0) {
        const firstCount = this.formationsParAnnee[0]?.count || 1;
        const lastCount  = this.formationsParAnnee[this.formationsParAnnee.length - 1].count;
        const growthPct  = Math.round((lastCount / firstCount) * 100);
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(8);
        doc.setTextColor(16, 185, 129);
        doc.text(`Croissance annuelle: +${growthPct}%`, 20 + halfW / 2, 118, { align: 'center' });
      }
    }

    // ✅ Capture haute resolution graphique Domaine
    const imgDomaine = await this.captureChart('chartDomaine');
    if (imgDomaine) {
      doc.addImage(imgDomaine, 'PNG', xDomaine, 20, halfW, 80);

      doc.setFont('helvetica', 'bold');
      doc.setFontSize(10);
      doc.setTextColor(71, 85, 105);
      doc.text("Repartition par domaine", xDomaine + halfW / 2, 108, { align: 'center' });
    }

    // Bandeau domaine leader
    const topDomaine    = this.getTopDomaine();
    const topDomaineData = this.formationsParDomaine.find(d => d.label === topDomaine);
    const topPct        = topDomaineData ? Math.round((topDomaineData.count / this.totaux.formations) * 100) : 0;

    const bandeauY = 130;
    doc.setFillColor(245, 158, 11);
    doc.roundedRect(20, bandeauY, pageWidth - 40, 30, 5, 5, 'F');
    doc.setFillColor(255, 255, 255);
    doc.triangle(32, bandeauY + 20, 38, bandeauY + 8, 44, bandeauY + 20, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.setTextColor(255, 255, 255);
    doc.text(`DOMAINE LEADER: ${topDomaine}`, pageWidth / 2, bandeauY + 13, { align: 'center' });
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text(`${topPct}% des formations totales`, pageWidth / 2, bandeauY + 24, { align: 'center' });

    this.addFooter(doc, doc.getCurrentPageInfo().pageNumber, pageWidth, pageHeight);
  }

  private async addStructureInsightsPageV2(doc: jsPDF, pageWidth: number, pageHeight: number): Promise<void> {
    doc.addPage();

    doc.setFillColor(102, 126, 234);
    doc.rect(0, 0, pageWidth, 12, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(16);
    doc.setTextColor(255, 255, 255);
    doc.text("PARTICIPANTS & RECOMMANDATIONS", 20, 9);

    // ✅ Capture haute resolution graphique Structure
    const imgStructure = await this.captureChart('chartStructure');
    if (imgStructure) {
      doc.addImage(imgStructure, 'PNG', 20, 20, pageWidth - 40, 85);
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(10);
      doc.setTextColor(71, 85, 105);
      doc.text("Participants par structure", pageWidth / 2, 113, { align: 'center' });
    }

    // Section recommandations
    const recY = 124;
    doc.setFillColor(102, 126, 234);
    doc.roundedRect(20, recY, pageWidth - 40, 55, 5, 5, 'F');

    // Icone ampoule simplifiee
    doc.setFillColor(255, 255, 255);
    doc.roundedRect(27, recY + 8, 14, 14, 2, 2, 'F');
    doc.setFillColor(102, 126, 234);
    doc.circle(34, recY + 13, 3, 'F');
    doc.rect(31, recY + 16, 6, 3, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(11);
    doc.setTextColor(255, 255, 255);
    doc.text("RECOMMANDATIONS STRATEGIQUES", 48, recY + 17);

    doc.setDrawColor(255, 255, 255);
    doc.setLineWidth(0.3);
    doc.line(27, recY + 24, pageWidth - 25, recY + 24);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(255, 255, 255);

    const recs = [
      "Renforcer les formations dans le domaine leader pour capitaliser sur la demande.",
      "Augmenter la capacite d'accueil pendant les periodes de forte affluence.",
      "Developper de nouvelles formations pour diversifier l'offre.",
      "Ameliorer le taux de participation via des actions de communication ciblees."
    ];

    recs.forEach((rec, i) => {
      doc.setFillColor(255, 255, 255);
      doc.circle(34, recY + 33 + i * 9, 3.5, 'F');
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(8);
      doc.setTextColor(102, 126, 234);
      doc.text((i + 1).toString(), 34, recY + 33 + i * 9 + 1, { align: 'center' });

      doc.setFont('helvetica', 'normal');
      doc.setFontSize(9);
      doc.setTextColor(255, 255, 255);
      doc.text(rec, 42, recY + 33 + i * 9 + 1);
    });

    // Pied de page rapport
    doc.setFont('helvetica', 'italic');
    doc.setFontSize(8);
    doc.setTextColor(148, 163, 184);
    doc.text(
      "Excellent Training - Engage pour l'excellence de la formation professionnelle",
      pageWidth / 2, pageHeight - 20, { align: 'center' }
    );

    this.addFooter(doc, doc.getCurrentPageInfo().pageNumber, pageWidth, pageHeight);
  }

  private addFooter(doc: jsPDF, pageNum: number, pageWidth: number, pageHeight: number): void {
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8);
    doc.setTextColor(148, 163, 184);
    doc.text(
      `Excellent Training - Rapport genere le ${this.today.toLocaleDateString('fr-FR')}`,
      pageWidth / 2, pageHeight - 6, { align: 'center' }
    );
    doc.text(`Page ${pageNum}`, pageWidth - 20, pageHeight - 6);
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // ==================== CHARTS ====================

  private createAllCharts(): void {
    const evolutionCanvas = document.getElementById('chartEvolution');
    const anneeCanvas     = document.getElementById('chartAnnee');
    const domaineCanvas   = document.getElementById('chartDomaine');
    const structureCanvas = document.getElementById('chartStructure');

    if (!evolutionCanvas && !anneeCanvas && !domaineCanvas && !structureCanvas) {
      setTimeout(() => {
        if (!this.isDestroyed) this.createAllCharts();
      }, 100);
      return;
    }

    this.createEvolutionChart();
    this.createAnneeChart();
    this.createDomaineChart();
    this.createStructureChart();
  }

  private destroyCharts(): void {
    if (this.chartEvolution) { this.chartEvolution.destroy(); this.chartEvolution = null; }
    if (this.chartAnnee)     { this.chartAnnee.destroy();     this.chartAnnee     = null; }
    if (this.chartDomaine)   { this.chartDomaine.destroy();   this.chartDomaine   = null; }
    if (this.chartStructure) { this.chartStructure.destroy(); this.chartStructure = null; }
  }

  private createEvolutionChart(): void {
    const canvas = document.getElementById('chartEvolution') as HTMLCanvasElement;
    if (!canvas) return;
    if (this.chartEvolution) this.chartEvolution.destroy();

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
          y: { beginAtZero: true },
          x: { grid: { display: false } }
        }
      }
    });
  }

  private createAnneeChart(): void {
    const canvas = document.getElementById('chartAnnee') as HTMLCanvasElement;
    if (!canvas || this.formationsParAnnee.length === 0) return;
    if (this.chartAnnee) this.chartAnnee.destroy();

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
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true } }
      }
    });
  }

  private createDomaineChart(): void {
    const canvas = document.getElementById('chartDomaine') as HTMLCanvasElement;
    if (!canvas || this.formationsParDomaine.length === 0) return;
    if (this.chartDomaine) this.chartDomaine.destroy();

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
          legend: { position: 'bottom', labels: { font: { size: 10 }, usePointStyle: true } },
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
    if (this.chartStructure) this.chartStructure.destroy();

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
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: Math.ceil(maxVal / 5) } },
          x: { grid: { display: false } }
        }
      }
    });
  }

  getTopDomaine(): string {
    if (this.formationsParDomaine.length === 0) return 'Aucune donnee';
    return [...this.formationsParDomaine].sort((a, b) => b.count - a.count)[0].label;
  }

  getTopMois(): string {
    const mois = ['Janvier', 'Fevrier', 'Mars', 'Avril', 'Mai', 'Juin',
                  'Juillet', 'Aout', 'Septembre', 'Octobre', 'Novembre', 'Decembre'];
    const maxIdx = this.evolutionData.formations.indexOf(Math.max(...this.evolutionData.formations));
    return mois[maxIdx] || 'N/A';
  }
}