package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.FormateurDTO;
import com.isi.gestion_formation.model.Formateur;
import com.isi.gestion_formation.repository.iRepository.IEmployeurRepository;
import com.isi.gestion_formation.repository.iRepository.IFormateurRepository;
import com.isi.gestion_formation.service.iService.IFormateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        try {
            if (dto == null) {
                throw new IllegalArgumentException("Les données du formateur sont obligatoires");
            }

            // Validation des champs obligatoires
            if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom du formateur est obligatoire");
            }
            if (dto.getPrenom() == null || dto.getPrenom().trim().isEmpty()) {
                throw new IllegalArgumentException("Le prénom du formateur est obligatoire");
            }
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("L'email du formateur est obligatoire");
            }

            Formateur formateur = (dto.getId() != null)
                    ? repo.findById(dto.getId()).orElse(new Formateur())
                    : new Formateur();

            formateur.setNom(dto.getNom().trim());
            formateur.setPrenom(dto.getPrenom().trim());
            formateur.setEmail(dto.getEmail().trim());
            formateur.setTel(dto.getTel() != null ? dto.getTel().trim() : null);

            // Gestion du type (INTERNE / EXTERNE)
            if (dto.getType() != null && !dto.getType().isBlank()) {
                try {
                    formateur.setType(Formateur.TypeFormateur.valueOf(dto.getType().toUpperCase().trim()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Type de formateur invalide. Valeurs acceptées : INTERNE, EXTERNE");
                }
            }

            // Liaison avec l'employeur (si fourni)
            if (dto.getEmployeurId() != null) {
                formateur.setEmployeur(employeurRepo.findById(dto.getEmployeurId())
                        .orElseThrow(() -> new IllegalArgumentException("Employeur non trouvé avec l'ID : " + dto.getEmployeurId())));
            } else {
                formateur.setEmployeur(null); // Permet de dissocier si besoin
            }

            Formateur saved = repo.save(formateur);
            return toDTO(saved);

        } catch (IllegalArgumentException e) {
            throw e; // Erreur métier → 400 Bad Request
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du formateur : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FormateurDTO update(Long id, FormateurDTO dto) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la mise à jour");
            }
            dto.setId(id);
            return save(dto);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour du formateur : " + e.getMessage(), e);
        }
    }

    @Override
    public List<FormateurDTO> findAll() {
        try {
            return repo.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des formateurs : " + e.getMessage(), e);
        }
    }

    @Override
    public FormateurDTO findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire");
            }

            return repo.findById(id)
                    .map(this::toDTO)
                    .orElse(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du formateur : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la suppression");
            }

            if (!repo.existsById(id)) {
                throw new IllegalArgumentException("Formateur avec l'ID " + id + " non trouvé");
            }

            repo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du formateur : " + e.getMessage(), e);
        }
    }

    /**
     * Méthode utilitaire pour transformer l'entité Formateur en DTO
     */
    private FormateurDTO toDTO(Formateur f) {
        if (f == null) return null;

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