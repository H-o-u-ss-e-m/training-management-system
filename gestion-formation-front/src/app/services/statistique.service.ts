import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, shareReplay } from 'rxjs/operators';
import { StatDTO, Totaux } from '../models/statistique.model';

@Injectable({
  providedIn: 'root'
})
export class StatistiqueService {
  private apiUrl = 'http://localhost:8080/api/statistiques';

  constructor(private http: HttpClient) {}

  /**
   * Récupère les formations groupées par année
   */
  formationsParAnnee(): Observable<StatDTO[]> {
    return this.http.get<StatDTO[]>(`${this.apiUrl}/par-annee`).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des formations par année:', error);
        return of([]);
      })
    );
  }

  /**
   * Récupère les formations groupées par domaine
   */
  formationsParDomaine(): Observable<StatDTO[]> {
    return this.http.get<StatDTO[]>(`${this.apiUrl}/par-domaine`).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des formations par domaine:', error);
        return of([]);
      })
    );
  }

  /**
   * Récupère les formations groupées par structure
   */
  formationsParStructure(): Observable<StatDTO[]> {
    return this.http.get<StatDTO[]>(`${this.apiUrl}/par-structure`).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des formations par structure:', error);
        return of([]);
      })
    );
  }

  /**
   * Récupère les formations groupées par mois
   */
  formationsParMois(): Observable<StatDTO[]> {
    return this.http.get<StatDTO[]>(`${this.apiUrl}/par-mois`).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des formations par mois:', error);
        return of([]);
      })
    );
  }

  /**
   * Récupère les totaux (formations, participants, formateurs)
   */
  getTotaux(): Observable<Totaux> {
    return this.http.get<any>(`${this.apiUrl}/totaux`).pipe(
      map(response => ({
        formations: response.formations || 0,
        participants: response.participants || 0,
        formateurs: response.formateurs || 0
      })),
      catchError(error => {
        console.error('Erreur lors du chargement des totaux:', error);
        return of({ formations: 0, participants: 0, formateurs: 0 });
      }),
      shareReplay(1) // Cache le résultat
    );
  }

  /**
   * Récupère le taux de participation moyen
   */
  getTauxParticipation(): Observable<number> {
    return this.getTotaux().pipe(
      map(totaux => {
        if (totaux.formations === 0) return 0;
        const moyenne = totaux.participants / totaux.formations;
        return Math.min(Math.round((moyenne / 15) * 100), 100);
      }),
      catchError(() => of(0))
    );
  }
}