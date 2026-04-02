package com.isi.gestion_formation.service.iService;

import com.isi.gestion_formation.dto.ParticipantDTO;
import java.util.List;

public interface IParticipantService {
    ParticipantDTO save(ParticipantDTO dto);
    List<ParticipantDTO> findAll();
    ParticipantDTO findById(Long id);
    ParticipantDTO update(Long id, ParticipantDTO dto);
    void delete(Long id);
}