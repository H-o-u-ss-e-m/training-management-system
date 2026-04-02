package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Formation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface IFormationRepository extends JpaRepository<Formation, Long> {

    @Query("SELECT f.annee, COUNT(f) FROM Formation f GROUP BY f.annee ORDER BY f.annee")
    List<Object[]> countByAnnee();

    @Query("SELECT d.libelle, COUNT(f) FROM Formation f JOIN f.domaine d GROUP BY d.libelle")
    List<Object[]> countByDomaine();

    @Query("SELECT s.libelle, COUNT(DISTINCT p) FROM Participant p JOIN p.formations f JOIN p.structure s GROUP BY s.libelle")
    List<Object[]> countByStructure();
}