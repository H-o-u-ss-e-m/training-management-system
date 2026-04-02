export interface Formateur {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  tel: string;
  type: string;
  employeurId?: number;
  employeurNom?: string;
}

export enum TypeFormateur {
  INTERNE = 'INTERNE',
  EXTERNE = 'EXTERNE'
}
