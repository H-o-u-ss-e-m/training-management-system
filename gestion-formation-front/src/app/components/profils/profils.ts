import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfilService } from '../../services/profil.service';
import { Profil } from '../../models/profil.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-profils',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './profils.html',
  styleUrls: ['./profils.css']
})
export class ProfilsComponent implements OnInit {
  profils: Profil[] = [];
  isLoading = true;
  showModal = false;
  isEditMode = false;
  searchTerm = '';
  currentProfil: Profil = { libelle: '' };

  constructor(private profilService: ProfilService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadProfils(); }

  loadProfils(): void {
    this.isLoading = true;
    this.profilService.getAll().subscribe({
      next: (data) => { this.profils = data; this.isLoading = false; this.cdr.detectChanges(); },
      error: (err) => { console.error('Erreur:', err); this.isLoading = false; this.cdr.detectChanges(); }
    });
  }

  get filteredProfils(): Profil[] {
    if (!this.searchTerm) return this.profils;
    const term = this.searchTerm.toLowerCase();
    return this.profils.filter(p => p.libelle.toLowerCase().includes(term));
  }

  openAddModal(): void { this.isEditMode = false; this.currentProfil = { libelle: '' }; this.showModal = true; }
  openEditModal(profil: Profil): void { this.isEditMode = true; this.currentProfil = { ...profil }; this.showModal = true; }
  closeModal(): void { this.showModal = false; this.currentProfil = { libelle: '' }; }

  onSubmit(): void {
    if (this.isEditMode && this.currentProfil.id) {
      this.profilService.update(this.currentProfil.id, this.currentProfil).subscribe({
        next: () => { this.loadProfils(); this.closeModal(); },
        error: (err) => console.error('Erreur mise à jour:', err)
      });
    } else {
      this.profilService.create(this.currentProfil).subscribe({
        next: () => { this.loadProfils(); this.closeModal(); },
        error: (err) => console.error('Erreur création:', err)
      });
    }
  }

  deleteProfil(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce profil ?')) {
      this.profilService.delete(id).subscribe({ next: () => this.loadProfils(), error: (err) => console.error('Erreur suppression:', err) });
    }
  }
}