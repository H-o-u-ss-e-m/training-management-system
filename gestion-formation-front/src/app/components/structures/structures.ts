import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StructureService } from '../../services/structure.service';
import { Structure } from '../../models/structure.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-structures',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './structures.html',
  styleUrls: ['./structures.css']
})
export class StructuresComponent implements OnInit {
  structures: Structure[] = [];
  isLoading = true;
  showModal = false;
  isEditMode = false;
  searchTerm = '';
  currentStructure: Structure = { libelle: '' };

  constructor(
    private structureService: StructureService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void { this.loadStructures(); }

  loadStructures(): void {
    this.isLoading = true;
    this.structureService.getAll().subscribe({
      next: (data) => { this.structures = data; this.isLoading = false; this.cdr.detectChanges(); },
      error: (err) => { console.error('Erreur:', err); this.isLoading = false; this.cdr.detectChanges(); }
    });
  }

  get filteredStructures(): Structure[] {
    if (!this.searchTerm) return this.structures;
    const term = this.searchTerm.toLowerCase();
    return this.structures.filter(s => s.libelle.toLowerCase().includes(term));
  }

  openAddModal(): void { this.isEditMode = false; this.currentStructure = { libelle: '' }; this.showModal = true; }
  openEditModal(structure: Structure): void { this.isEditMode = true; this.currentStructure = { ...structure }; this.showModal = true; }
  closeModal(): void { this.showModal = false; this.currentStructure = { libelle: '' }; }

  onSubmit(): void {
    if (this.isEditMode && this.currentStructure.id) {
      this.structureService.update(this.currentStructure.id, this.currentStructure).subscribe({
        next: () => { this.loadStructures(); this.closeModal(); },
        error: (err) => console.error('Erreur mise à jour:', err)
      });
    } else {
      this.structureService.create(this.currentStructure).subscribe({
        next: () => { this.loadStructures(); this.closeModal(); },
        error: (err) => console.error('Erreur création:', err)
      });
    }
  }

  deleteStructure(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette structure ?')) {
      this.structureService.delete(id).subscribe({ next: () => this.loadStructures(), error: (err) => console.error('Erreur suppression:', err) });
    }
  }
}