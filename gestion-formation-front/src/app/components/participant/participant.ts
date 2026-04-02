import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ParticipantService } from '../../services/participant.service';
import { StructureService } from '../../services/structure.service';
import { ProfilService } from '../../services/profil.service';
import { Participant } from '../../models/participant.model';
import { Structure } from '../../models/structure.model';
import { Profil } from '../../models/profil.model';
import { NavbarComponent } from '../shared/navbar/navbar';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-participant',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './participant.html',
  styleUrls: ['./participant.css']
})
export class ParticipantComponent implements OnInit {
  participants: Participant[] = [];
  structures: Structure[] = [];
  profils: Profil[] = [];

  isLoading = true;
  showModal = false;
  isEditMode = false;
  searchTerm = '';

  currentParticipant: Participant = this.getEmptyParticipant();

  constructor(
    private participantService: ParticipantService,
    private structureService: StructureService,
    private profilService: ProfilService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('ParticipantComponent initialisé');
    this.loadAllData();
  }

  private loadAllData(): void {
    this.isLoading = true;

    Promise.all([
      this.participantService.getAll().toPromise(),
      this.structureService.getAll().toPromise(),
      this.profilService.getAll().toPromise()
    ]).then(([participants, structures, profils]) => {
      this.participants = participants || [];
      this.structures = structures || [];
      this.profils = profils || [];
      this.isLoading = false;
      this.cdr.detectChanges();
      console.log('Données participants chargées:', {
        participants: this.participants.length,
        structures: this.structures.length,
        profils: this.profils.length
      });
    }).catch(err => {
      console.error('Erreur chargement données participants:', err);
      this.isLoading = false;
      this.cdr.detectChanges();
    });
  }

  get filteredParticipants(): Participant[] {
    if (!this.searchTerm) return this.participants;
    const term = this.searchTerm.toLowerCase();
    return this.participants.filter(p =>
      p.nom.toLowerCase().includes(term) ||
      p.prenom.toLowerCase().includes(term) ||
      p.email.toLowerCase().includes(term) ||
      (p.structureLibelle || '').toLowerCase().includes(term)
    );
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.currentParticipant = this.getEmptyParticipant();
    this.showModal = true;
    this.cdr.detectChanges();
  }

  openEditModal(participant: Participant): void {
    this.isEditMode = true;
    this.currentParticipant = { ...participant };
    this.showModal = true;
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.showModal = false;
    this.currentParticipant = this.getEmptyParticipant();
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.isEditMode && this.currentParticipant.id) {
      this.participantService.update(this.currentParticipant.id, this.currentParticipant).subscribe({
        next: () => {
          console.log('Participant mis à jour avec succès');
          this.loadAllData();
          this.closeModal();
        },
        error: (error) => {
          console.error('Erreur mise à jour:', error);
          alert('Erreur lors de la mise à jour du participant');
        }
      });
    } else {
      this.participantService.create(this.currentParticipant).subscribe({
        next: () => {
          console.log('Participant créé avec succès');
          this.loadAllData();
          this.closeModal();
        },
        error: (error) => {
          console.error('Erreur création:', error);
          alert('Erreur lors de la création du participant');
        }
      });
    }
  }

  deleteParticipant(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce participant ?')) {
      this.participantService.delete(id).subscribe({
        next: () => {
          console.log('Participant supprimé avec succès');
          this.loadAllData();
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
          alert('Erreur lors de la suppression du participant');
        }
      });
    }
  }

  private getEmptyParticipant(): Participant {
    return {
      nom: '',
      prenom: '',
      email: '',
      tel: 0,
      structureId: 0,
      profilId: 0
    };
  }
}