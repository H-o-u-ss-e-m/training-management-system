# 📘 Gestion Formation — Guide Développeurs

> **Stack** : Spring Boot · Angular · PostgreSQL  
> Suis ce guide étape par étape pour lancer le projet en local.

---

## 📋 Table des matières

1. [Cloner le projet](#1️⃣-cloner-le-projet)
2. [Backend Spring Boot](#2️⃣-backend-spring-boot)
3. [Frontend Angular](#3️⃣-frontend-angular)
4. [Base de données PostgreSQL](#4️⃣-base-de-données-postgresql)
5. [Partager les changements](#5️⃣-partager-les-changements-base-de-données)
6. [Bonnes pratiques](#6️⃣-bonnes-pratiques)
7. [Liens utiles](#7️⃣-liens-utiles)

---

## 1️⃣ Cloner le projet

```bash
git clone https://github.com/TON-UTILISATEUR/TON-PROJET.git
cd TON-PROJET
```

---

## 2️⃣ Backend Spring Boot

### Prérequis
- ✅ Java 17+ installé
- ✅ IntelliJ IDEA (recommandé) ou tout IDE Maven

### Configuration — `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/gestion_formation
    username: postgres
    password: system
```

> ⚠️ Si tu changes le nom de la base, modifie aussi `gestion_formation` ici.

### Lancer le backend

**Depuis IntelliJ :**
> Clic droit sur `GestionFormationApplication.java` → **Run**

**Depuis le terminal :**
```bash
./mvnw spring-boot:run
```

✅ Backend disponible sur **http://localhost:8080**

---

## 3️⃣ Frontend Angular

### Prérequis
- ✅ Node.js et npm installés

### Installation et lancement

```bash
cd frontend-angular
npm install
ng serve
```

✅ Frontend disponible sur **http://localhost:4200**

> ⚠️ Assure-toi que le **backend est lancé** avant d'ouvrir le frontend.

---

## 4️⃣ Base de données PostgreSQL

### Étape A — Créer la base de données

Ouvre **SQL Shell (psql)** et connecte-toi avec l'utilisateur `postgres`, puis :

```sql
CREATE DATABASE gestion_formation;
```

### Étape B — Importer le dump SQL

Ouvre un terminal **(CMD)**, place-toi dans le dossier du projet et exécute :

```bash
psql -U postgres -h localhost -d gestion_formation < gestion_formation_dump.sql
```

> 💡 Le fichier `gestion_formation_dump.sql` doit être dans le dossier courant, ou indique le chemin complet.

---

## 5️⃣ Partager les changements (base de données)

### Étape A — Modifier la base
Fais tes modifications via **SQL Shell**, **pgAdmin** ou directement depuis l'application.

### Étape B — Créer un nouveau dump

```bash
pg_dump -U postgres -h localhost -d gestion_formation > gestion_formation_dump.sql
```

### Étape C — Pousser sur Git

```bash
git add gestion_formation_dump.sql
git commit -m "Mise à jour de la base avec les dernières modifications"
git push origin main
```

### Étape D — Récupérer la dernière version (pour les autres développeurs)

```bash
git pull origin main
psql -U postgres -h localhost -d gestion_formation < gestion_formation_dump.sql
```

> ⚠️ **Toujours faire un `git pull` avant de `git push`** pour éviter d'écraser le travail des autres.

---

## 6️⃣ Bonnes pratiques

| ✅ À faire | ❌ À éviter |
|---|---|
| Travailler sur sa propre base locale | Modifier directement la base de prod |
| Partager uniquement les changements nécessaires | Pusher sans avoir fait un `pull` avant |
| Vérifier que `application.yml` correspond au dump importé | Mettre des mots de passe dans un repo public |
| Commenter ses commits clairement | Laisser des données sensibles dans `.env` ou `application.yml` |

---

## 7️⃣ Liens utiles

- 🐘 [PostgreSQL](https://www.postgresql.org/)
- 🅰️ [Angular CLI](https://angular.io/cli)
- 🍃 [Spring Boot](https://spring.io/projects/spring-boot)

---

<div align="center">

**🚀 Lancement rapide en 3 étapes**

```
1. psql → CREATE DATABASE gestion_formation;
2. psql -U postgres -d gestion_formation < gestion_formation_dump.sql
3. ./mvnw spring-boot:run  +  ng serve
```

</div>
