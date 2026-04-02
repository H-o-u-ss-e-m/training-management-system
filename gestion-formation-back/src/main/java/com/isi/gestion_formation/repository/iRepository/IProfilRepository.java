package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProfilRepository extends JpaRepository<Profil, Long> {}