import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomaineService } from '../../services/domaine.service';
import { Domaine } from '../../models/domaine.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-domaines',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './domaines.html',
  styleUrls: ['./domaines.css']
})
export class DomainesComponent implements OnInit {
  domaines: Domaine[] = [];
  isLoading = true;
  showModal = false;
  isEditMode = false;
  searchTerm = '';
  currentDomaine: Domaine = { libelle: '' };

  constructor(
    private domaineService: DomaineService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDomaines();
  }

  loadDomaines(): void {
    this.isLoading = true;
    this.domaineService.getAll().subscribe({
      next: (data) => {
        this.domaines = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement domaines:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get filteredDomaines(): Domaine[] {
    if (!this.searchTerm) return this.domaines;
    const term = this.searchTerm.toLowerCase();
    return this.domaines.filter(d => d.libelle.toLowerCase().includes(term));
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.currentDomaine = { libelle: '' };
    this.showModal = true;
  }

  openEditModal(domaine: Domaine): void {
    this.isEditMode = true;
    this.currentDomaine = { ...domaine };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentDomaine = { libelle: '' };
  }

  onSubmit(): void {
    if (this.isEditMode && this.currentDomaine.id) {
      this.domaineService.update(this.currentDomaine.id, this.currentDomaine).subscribe({
        next: () => {
          this.loadDomaines();
          this.closeModal();
        },
        error: (err) => console.error('Erreur mise à jour:', err)
      });
    } else {
      this.domaineService.create(this.currentDomaine).subscribe({
        next: () => {
          this.loadDomaines();
          this.closeModal();
        },
        error: (err) => console.error('Erreur création:', err)
      });
    }
  }

  deleteDomaine(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce domaine ?')) {
      this.domaineService.delete(id).subscribe({
        next: () => this.loadDomaines(),
        error: (err) => console.error('Erreur suppression:', err)
      });
    }
  }
}