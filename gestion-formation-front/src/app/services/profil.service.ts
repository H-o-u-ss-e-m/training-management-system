import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Profil } from '../models/profil.model';

@Injectable({
  providedIn: 'root'
})
export class ProfilService {
  private apiUrl = 'http://localhost:8080/api/profils';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Profil[]> {
    return this.http.get<Profil[]>(`${this.apiUrl}/all`);
  }

  getById(id: number): Observable<Profil> {
    return this.http.get<Profil>(`${this.apiUrl}/${id}`);
  }

  create(profil: Profil): Observable<Profil> {
    return this.http.post<Profil>(`${this.apiUrl}/save`, profil);
  }

  update(id: number, profil: Profil): Observable<Profil> {
    return this.http.put<Profil>(`${this.apiUrl}/update/${id}`, profil);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}
