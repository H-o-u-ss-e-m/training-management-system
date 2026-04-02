import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UtilisateurService } from '../../services/utilisateur.service';
import { Utilisateur } from '../../models/utilisateur.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './utilisateurs.html',
  styleUrls: ['./utilisateurs.css']
})
export class UtilisateursComponent implements OnInit {
  utilisateurs: Utilisateur[] = [];
  isLoading = true;
  showModal = false;
  isEditMode = false;
  searchTerm = '';
  currentUtilisateur: Utilisateur = { login: '', roleId: 0 };
  roles = ['SIMPLE_UTILISATEUR', 'RESPONSABLE', 'ADMINISTRATEUR'];

  constructor(private utilisateurService: UtilisateurService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadUtilisateurs(); }

  loadUtilisateurs(): void {
    this.isLoading = true;
    this.utilisateurService.getAll().subscribe({
      next: (data) => { this.utilisateurs = data; this.isLoading = false; this.cdr.detectChanges(); },
      error: (err) => { console.error('Erreur:', err); this.isLoading = false; this.cdr.detectChanges(); }
    });
  }

  get filteredUtilisateurs(): Utilisateur[] {
    if (!this.searchTerm) return this.utilisateurs;
    const term = this.searchTerm.toLowerCase();
    return this.utilisateurs.filter(u => u.login.toLowerCase().includes(term) || (u.roleNom || '').toLowerCase().includes(term));
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.currentUtilisateur = { login: '', password: '', roleId: 0 };
    this.showModal = true;
  }

  openEditModal(utilisateur: Utilisateur): void {
    this.isEditMode = true;
    this.currentUtilisateur = { ...utilisateur };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentUtilisateur = { login: '', roleId: 0 };
  }

  onSubmit(): void {
    if (this.isEditMode && this.currentUtilisateur.id) {
      this.utilisateurService.update(this.currentUtilisateur.id, this.currentUtilisateur).subscribe({
        next: () => { this.loadUtilisateurs(); this.closeModal(); },
        error: (err) => console.error('Erreur mise à jour:', err)
      });
    } else {
      this.utilisateurService.create(this.currentUtilisateur).subscribe({
        next: () => { this.loadUtilisateurs(); this.closeModal(); },
        error: (err) => console.error('Erreur création:', err)
      });
    }
  }

  deleteUtilisateur(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.utilisateurService.delete(id).subscribe({ next: () => this.loadUtilisateurs(), error: (err) => console.error('Erreur suppression:', err) });
    }
  }
}