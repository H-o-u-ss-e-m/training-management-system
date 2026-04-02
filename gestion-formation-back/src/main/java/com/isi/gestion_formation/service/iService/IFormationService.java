package com.isi.gestion_formation.service.iService;

import com.isi.gestion_formation.dto.FormationDTO;
import java.util.List;

public interface IFormationService {
    FormationDTO save(FormationDTO dto);
    FormationDTO update(Long id, FormationDTO dto);
    List<FormationDTO> findAll();
    FormationDTO findById(Long id);
    void delete(Long id);
}