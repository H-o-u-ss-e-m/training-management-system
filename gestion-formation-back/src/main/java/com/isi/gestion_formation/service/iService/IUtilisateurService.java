package com.isi.gestion_formation.service.iService;

import com.isi.gestion_formation.dto.UtilisateurDTO;
import java.util.List;

public interface IUtilisateurService {
    UtilisateurDTO login(String login, String password);
    UtilisateurDTO save(UtilisateurDTO dto);
    List<UtilisateurDTO> findAll();
    UtilisateurDTO update(Long id, UtilisateurDTO dto);
    UtilisateurDTO findById(Long id);
    void delete(Long id);
}