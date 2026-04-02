package com.isi.gestion_formation.service.iService;

import com.isi.gestion_formation.dto.FormateurDTO;
import java.util.List;

public interface IFormateurService {
    FormateurDTO save(FormateurDTO dto);
    List<FormateurDTO> findAll();
    FormateurDTO findById(Long id);
    FormateurDTO update(Long id, FormateurDTO dto);
    void delete(Long id);
}