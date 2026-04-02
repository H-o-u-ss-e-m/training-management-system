package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStructureRepository extends JpaRepository<Structure, Long> {}