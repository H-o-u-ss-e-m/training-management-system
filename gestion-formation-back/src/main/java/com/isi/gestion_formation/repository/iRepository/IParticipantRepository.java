package com.isi.gestion_formation.repository.iRepository;

import com.isi.gestion_formation.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByFormationsId(Long formationId);
}