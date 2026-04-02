package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUtilisateurRepository extends JpaRepository<Utilisateur, Long> { Optional<Utilisateur> findByLogin(String login); }
