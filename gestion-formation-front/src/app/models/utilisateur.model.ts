export interface Utilisateur {
  id?: number;
  login: string;
  password?: string;
  roleId?: number; // Ajoute le ? ici
  roleNom?: string;
}