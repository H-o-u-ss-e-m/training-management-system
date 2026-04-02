import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Participant } from '../models/participant.model';

@Injectable({
  providedIn: 'root'
})
export class ParticipantService {
  private apiUrl = 'http://localhost:8080/api/participants';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.apiUrl}/all`);
  }

  getById(id: number): Observable<Participant> {
    return this.http.get<Participant>(`${this.apiUrl}/${id}`);
  }

  create(participant: Participant): Observable<Participant> {
    return this.http.post<Participant>(`${this.apiUrl}/save`, participant);
  }

  update(id: number, participant: Participant): Observable<Participant> {
    return this.http.put<Participant>(`${this.apiUrl}/update/${id}`, participant);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}
