import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StatDTO, Totaux } from '../models/statistique.model';

@Injectable({
  providedIn: 'root'
})
export class StatistiqueService {
  private apiUrl = 'http://localhost:8080/api/statistiques';

  constructor(private http: HttpClient) {}

  formationsParAnnee(): Observable<StatDTO[]> {
    return this.http.get<StatDTO[]>(`${this.apiUrl}/par-annee`);
  }

  formationsParDomaine(): Observable<StatDTO[]> {
    return this.http.get<StatDTO[]>(`${this.apiUrl}/par-domaine`);
  }

  formationsParStructure(): Observable<StatDTO[]> {
    return this.http.get<StatDTO[]>(`${this.apiUrl}/par-structure`);
  }

  getTotaux(): Observable<Totaux> {
    return this.http.get<Totaux>(`${this.apiUrl}/totaux`);
  }
}
