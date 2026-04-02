import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { DashboardComponent } from './components/dashboard/dashboard';
import { FormationComponent } from './components/formation/formation';
import { ParticipantComponent } from './components/participant/participant';
import { FormateurComponent } from './components/formateur/formateur';
import { DomainesComponent } from './components/domaines/domaines';
import { EmployeursComponent } from './components/employeurs/employeurs';
import { ProfilsComponent } from './components/profils/profils';
import { StructuresComponent } from './components/structures/structures';
import { UtilisateursComponent } from './components/utilisateurs/utilisateurs';
import { StatistiquesComponent } from './components/statistiques/statistiques';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'formations', component: FormationComponent },
  { path: 'participants', component: ParticipantComponent },
  { path: 'formateurs', component: FormateurComponent },
  { path: 'domaines', component: DomainesComponent },
  { path: 'structures', component: StructuresComponent },
  { path: 'profils', component: ProfilsComponent },
  { path: 'employeurs', component: EmployeursComponent },
  { path: 'utilisateurs', component: UtilisateursComponent },
  { path: 'statistiques', component: StatistiquesComponent },
  // Redirection par défaut
  { path: '**', redirectTo: '/dashboard' }
];