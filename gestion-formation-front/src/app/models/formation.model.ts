export interface Formation {
  id?: number;
  titre: string;
  annee: number;
  duree: number;
  budget: number;
  lieu: string;
  dateFormation: string;
  domaineId: number;
  domaineLibelle?: string;
  formateurId?: number;
  participantIds?: number[];
}
