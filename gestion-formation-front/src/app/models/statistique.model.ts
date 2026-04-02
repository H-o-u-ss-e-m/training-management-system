export interface StatDTO {
  label: string;
  count: number;
}

export interface Totaux {
  formations: number;
  participants: number;
  formateurs: number;
}

export interface EvolutionData {
  mois: string;
  formations: number;
  participants: number;
}