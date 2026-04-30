package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByFormationsId(Long formationId);

    @Query("select p from Participant p where size(p.formations) > 0")
    List<Participant> findParticipantsAyantDejaParticipe();

    @Query("select p from Participant p where size(p.formations) = 0")
    List<Participant> findParticipantsJamaisParticipe();
}