package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Employeur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEmployeurRepository extends JpaRepository<Employeur, Long> {}