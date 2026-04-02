import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeurService } from '../../services/employeur.service';
import { Employeur } from '../../models/employeur.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-employeurs',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './employeurs.html',
  styleUrls: ['./employeurs.css']
})
export class EmployeursComponent implements OnInit {
  employeurs: Employeur[] = [];
  isLoading = true;
  showModal = false;
  isEditMode = false;
  searchTerm = '';
  currentEmployeur: Employeur = { libelle: '' };

  constructor(private employeurService: EmployeurService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadEmployeurs(); }

  loadEmployeurs(): void {
    this.isLoading = true;
    this.employeurService.getAll().subscribe({
      next: (data) => { this.employeurs = data; this.isLoading = false; this.cdr.detectChanges(); },
      error: (err) => { console.error('Erreur:', err); this.isLoading = false; this.cdr.detectChanges(); }
    });
  }

  get filteredEmployeurs(): Employeur[] {
    if (!this.searchTerm) return this.employeurs;
    const term = this.searchTerm.toLowerCase();
    return this.employeurs.filter(e => e.libelle.toLowerCase().includes(term));
  }

  openAddModal(): void { this.isEditMode = false; this.currentEmployeur = { libelle: '' }; this.showModal = true; }
  openEditModal(employeur: Employeur): void { this.isEditMode = true; this.currentEmployeur = { ...employeur }; this.showModal = true; }
  closeModal(): void { this.showModal = false; this.currentEmployeur = { libelle: '' }; }

  onSubmit(): void {
    if (this.isEditMode && this.currentEmployeur.id) {
      this.employeurService.update(this.currentEmployeur.id, this.currentEmployeur).subscribe({
        next: () => { this.loadEmployeurs(); this.closeModal(); },
        error: (err) => console.error('Erreur mise à jour:', err)
      });
    } else {
      this.employeurService.create(this.currentEmployeur).subscribe({
        next: () => { this.loadEmployeurs(); this.closeModal(); },
        error: (err) => console.error('Erreur création:', err)
      });
    }
  }

  deleteEmployeur(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet employeur ?')) {
      this.employeurService.delete(id).subscribe({ next: () => this.loadEmployeurs(), error: (err) => console.error('Erreur suppression:', err) });
    }
  }
}