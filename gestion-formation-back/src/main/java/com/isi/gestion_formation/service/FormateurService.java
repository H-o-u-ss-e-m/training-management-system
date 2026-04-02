package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.FormateurDTO;
import com.isi.gestion_formation.model.Formateur;
import com.isi.gestion_formation.repository.iRepository.IEmployeurRepository;
import com.isi.gestion_formation.repository.iRepository.IFormateurRepository;
import com.isi.gestion_formation.service.iService.IFormateurService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormateurService implements IFormateurService {

    private final IFormateurRepository repo;
    private final IEmployeurRepository employeurRepo;

    @Override
    @Transactional
    public FormateurDTO save(FormateurDTO dto) {
        // Chargement si existant (pour Update) ou création d'un nouveau
        Formateur f = (dto.getId() != null) ?
                repo.findById(dto.getId()).orElse(new Formateur()) : new Formateur();

        f.setNom(dto.getNom());
        f.setPrenom(dto.getPrenom());
        f.setEmail(dto.getEmail());
        f.setTel(dto.getTel());

        // Conversion sécurisée du type (INTERNE / EXTERNE)
        if (dto.getType() != null && !dto.getType().isBlank()) {
            f.setType(Formateur.TypeFormateur.valueOf(dto.getType().toUpperCase().trim()));
        }

        // Liaison avec l'employeur
        if (dto.getEmployeurId() != null) {
            f.setEmployeur(employeurRepo.findById(dto.getEmployeurId())
                    .orElseThrow(() -> new RuntimeException("Employeur non trouvé avec l'ID : " + dto.getEmployeurId())));
        }

        return toDTO(repo.save(f));
    }

    @Override
    @Transactional
    public FormateurDTO update(Long id, FormateurDTO dto) {
        dto.setId(id);
        return save(dto);
    }

    @Override
    public List<FormateurDTO> findAll() {
        return repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FormateurDTO findById(Long id) {
        return repo.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    /**
     * Méthode utilitaire pour transformer l'entité Formateur en DTO
     */
    private FormateurDTO toDTO(Formateur f) {
        FormateurDTO dto = new FormateurDTO();
        dto.setId(f.getId());
        dto.setNom(f.getNom());
        dto.setPrenom(f.getPrenom());
        dto.setEmail(f.getEmail());
        dto.setTel(f.getTel());
        dto.setType(f.getType() != null ? f.getType().name() : null);

        if (f.getEmployeur() != null) {
            dto.setEmployeurId(f.getEmployeur().getId());
            dto.setEmployeurNom(f.getEmployeur().getNomEmployeur());
        }
        return dto;
    }
}