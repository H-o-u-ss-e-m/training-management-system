import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Formateur } from '../models/formateur.model';

@Injectable({
  providedIn: 'root'
})
export class FormateurService {
  private apiUrl = 'http://localhost:8080/api/formateurs';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Formateur[]> {
    return this.http.get<Formateur[]>(`${this.apiUrl}/all`);
  }

  getById(id: number): Observable<Formateur> {
    return this.http.get<Formateur>(`${this.apiUrl}/${id}`);
  }

  create(formateur: Formateur): Observable<Formateur> {
    return this.http.post<Formateur>(`${this.apiUrl}/save`, formateur);
  }

  update(id: number, formateur: Formateur): Observable<Formateur> {
    return this.http.put<Formateur>(`${this.apiUrl}/update/${id}`, formateur);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}
