package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Formation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IFormationRepository extends JpaRepository<Formation, Long> {


}