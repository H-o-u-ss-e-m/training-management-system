export interface Role {
  id: number;
  nom: string;
}

export enum RoleEnum {
  SIMPLE_UTILISATEUR = 'SIMPLE_UTILISATEUR',
  RESPONSABLE = 'RESPONSABLE',
  ADMINISTRATEUR = 'ADMINISTRATEUR'
}
