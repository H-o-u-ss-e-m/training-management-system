package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Formateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFormateurRepository extends JpaRepository<Formateur, Long> {}