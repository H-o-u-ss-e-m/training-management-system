import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormationService } from '../../services/formation.service';
import { DomaineService } from '../../services/domaine.service';
import { FormateurService } from '../../services/formateur.service';
import { ParticipantService } from '../../services/participant.service';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';
import { Domaine } from '../../models/domaine.model';
import { Formateur } from '../../models/formateur.model';
import { Formation } from '../../models/formation.model';
import { Participant } from '../../models/participant.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-formation',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './formation.html',
  styleUrls: ['./formation.css']
})
export class FormationComponent implements OnInit, OnDestroy {

  formations: Formation[] = [];
  domaines: Domaine[] = [];
  formateurs: Formateur[] = [];
  participants: Participant[] = [];

  isLoading = true;
  showModal = false;
  isEditMode = false;
  currentFormation: Formation = this.getEmptyFormation();
  selectedParticipants: number[] = [];
  searchTerm = '';
  
  private subscriptions: Subscription[] = [];

  constructor(
    private formationService: FormationService,
    private domaineService: DomaineService,
    private formateurService: FormateurService,
    private participantService: ParticipantService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('FormationComponent initialisé');
    this.loadAllData();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  private loadAllData(): void {
    this.isLoading = true;
    console.log('Chargement des données formations...');

    // Méthode 1: Avec Promise.all (plus simple)
    Promise.all([
      this.formationService.getAll().toPromise(),
      this.domaineService.getAll().toPromise(),
      this.formateurService.getAll().toPromise(),
      this.participantService.getAll().toPromise()
    ]).then(([formations, domaines, formateurs, participants]) => {
      this.formations = formations || [];
      this.domaines = domaines || [];
      this.formateurs = formateurs || [];
      this.participants = participants || [];
      this.isLoading = false;
      this.cdr.detectChanges();
      console.log('Données formations chargées:', {
        formations: this.formations.length,
        domaines: this.domaines.length,
        formateurs: this.formateurs.length,
        participants: this.participants.length
      });
    }).catch(err => {
      console.error('Erreur chargement données formations:', err);
      this.isLoading = false;
      this.cdr.detectChanges();
    });
  }

  // Filtre les formations par recherche
  get filteredFormations(): Formation[] {
    if (!this.searchTerm) return this.formations;
    const term = this.searchTerm.toLowerCase();
    return this.formations.filter(f =>
      f.titre.toLowerCase().includes(term) || 
      (f.domaineLibelle || '').toLowerCase().includes(term) ||
      (f.lieu || '').toLowerCase().includes(term)
    );
  }

  // Ouvre le modal d'ajout
  openAddModal(): void {
    this.isEditMode = false;
    this.currentFormation = this.getEmptyFormation();
    this.selectedParticipants = [];
    this.showModal = true;
    this.cdr.detectChanges();
  }

  // Ouvre le modal d'édition
  openEditModal(formation: Formation): void {
    this.isEditMode = true;
    this.currentFormation = { ...formation };
    this.selectedParticipants = formation.participantIds || [];
    this.showModal = true;
    this.cdr.detectChanges();
  }

  // Ferme le modal
  closeModal(): void {
    this.showModal = false;
    this.currentFormation = this.getEmptyFormation();
    this.selectedParticipants = [];
    this.cdr.detectChanges();
  }

  // Active/Désactive la sélection d'un participant
  toggleParticipant(id: number): void {
    const index = this.selectedParticipants.indexOf(id);
    if (index > -1) {
      this.selectedParticipants.splice(index, 1);
    } else {
      this.selectedParticipants.push(id);
    }
    this.cdr.detectChanges();
  }

  // Vérifie si un participant est sélectionné
  isParticipantSelected(id: number): boolean {
    return this.selectedParticipants.includes(id);
  }

  // Soumet le formulaire (ajout ou modification)
  onSubmit(): void {
    this.currentFormation.participantIds = this.selectedParticipants;

    if (this.isEditMode && this.currentFormation.id) {
      // Mode édition
      this.formationService.update(this.currentFormation.id, this.currentFormation).subscribe({
        next: () => {
          console.log('Formation mise à jour avec succès');
          this.loadAllData();
          this.closeModal();
        },
        error: (error) => {
          console.error('Erreur mise à jour:', error);
          alert('Erreur lors de la mise à jour de la formation');
        }
      });
    } else {
      // Mode ajout
      this.formationService.create(this.currentFormation).subscribe({
        next: () => {
          console.log('Formation créée avec succès');
          this.loadAllData();
          this.closeModal();
        },
        error: (error) => {
          console.error('Erreur création:', error);
          alert('Erreur lors de la création de la formation');
        }
      });
    }
  }

  // Supprime une formation
  deleteFormation(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette formation ?')) {
      this.formationService.delete(id).subscribe({
        next: () => {
          console.log('Formation supprimée avec succès');
          this.loadAllData();
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
          alert('Erreur lors de la suppression de la formation');
        }
      });
    }
  }

  // Retourne une formation vide
  private getEmptyFormation(): Formation {
    return {
      titre: '',
      annee: new Date().getFullYear(),
      duree: 1,
      budget: 0,
      lieu: '',
      dateFormation: '',
      domaineId: 0,
      participantIds: []
    };
  }
}