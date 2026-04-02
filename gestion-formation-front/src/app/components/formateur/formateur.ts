import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormateurService } from '../../services/formateur.service';
import { EmployeurService } from '../../services/employeur.service';
import { Formateur } from '../../models/formateur.model';
import { Employeur } from '../../models/employeur.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-formateur',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './formateur.html',
  styleUrls: ['./formateur.css']
})
export class FormateurComponent implements OnInit {
  formateurs: Formateur[] = [];
  employeurs: Employeur[] = [];

  isLoading = true;
  showModal = false;
  isEditMode = false;
  searchTerm = '';

  currentFormateur: Formateur = this.getEmptyFormateur();

  constructor(
    private formateurService: FormateurService,
    private employeurService: EmployeurService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('FormateurComponent initialisé');
    this.loadAllData();
  }

  private loadAllData(): void {
    this.isLoading = true;

    Promise.all([
      this.formateurService.getAll().toPromise(),
      this.employeurService.getAll().toPromise()
    ]).then(([formateurs, employeurs]) => {
      this.formateurs = formateurs || [];
      this.employeurs = employeurs || [];
      this.isLoading = false;
      this.cdr.detectChanges();
      console.log('Données formateurs chargées:', {
        formateurs: this.formateurs.length,
        employeurs: this.employeurs.length
      });
    }).catch(err => {
      console.error('Erreur chargement données formateurs:', err);
      this.isLoading = false;
      this.cdr.detectChanges();
    });
  }

  get filteredFormateurs(): Formateur[] {
    if (!this.searchTerm) return this.formateurs;
    const term = this.searchTerm.toLowerCase();
    return this.formateurs.filter(f =>
      f.nom.toLowerCase().includes(term) ||
      f.prenom.toLowerCase().includes(term) ||
      f.email.toLowerCase().includes(term) ||
      (f.employeurNom || '').toLowerCase().includes(term)
    );
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.currentFormateur = this.getEmptyFormateur();
    this.showModal = true;
    this.cdr.detectChanges();
  }

  openEditModal(formateur: Formateur): void {
    this.isEditMode = true;
    this.currentFormateur = { ...formateur };
    this.showModal = true;
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.showModal = false;
    this.currentFormateur = this.getEmptyFormateur();
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.isEditMode && this.currentFormateur.id) {
      this.formateurService.update(this.currentFormateur.id, this.currentFormateur).subscribe({
        next: () => {
          console.log('Formateur mis à jour avec succès');
          this.loadAllData();
          this.closeModal();
        },
        error: (error) => {
          console.error('Erreur mise à jour:', error);
          alert('Erreur lors de la mise à jour du formateur');
        }
      });
    } else {
      this.formateurService.create(this.currentFormateur).subscribe({
        next: () => {
          console.log('Formateur créé avec succès');
          this.loadAllData();
          this.closeModal();
        },
        error: (error) => {
          console.error('Erreur création:', error);
          alert('Erreur lors de la création du formateur');
        }
      });
    }
  }

  deleteFormateur(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce formateur ?')) {
      this.formateurService.delete(id).subscribe({
        next: () => {
          console.log('Formateur supprimé avec succès');
          this.loadAllData();
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
          alert('Erreur lors de la suppression du formateur');
        }
      });
    }
  }

  private getEmptyFormateur(): Formateur {
    return {
      nom: '',
      prenom: '',
      email: '',
      tel: '',
      type: 'INTERNE'
    };
  }
}