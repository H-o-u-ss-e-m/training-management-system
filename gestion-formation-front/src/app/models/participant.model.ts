export interface Participant {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  tel: number;
  structureId: number;
  structureLibelle?: string;
  profilId: number;
  profilLibelle?: string;
}
