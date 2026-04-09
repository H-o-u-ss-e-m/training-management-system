package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.ParticipantDTO;
import com.isi.gestion_formation.model.Participant;
import com.isi.gestion_formation.repository.iRepository.IParticipantRepository;
import com.isi.gestion_formation.repository.iRepository.IProfilRepository;
import com.isi.gestion_formation.repository.iRepository.IStructureRepository;
import com.isi.gestion_formation.service.iService.IParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService implements IParticipantService {

    private final IParticipantRepository repo;
    private final IStructureRepository structureRepo;
    private final IProfilRepository profilRepo;

    @Override
    @Transactional
    public ParticipantDTO save(ParticipantDTO dto) {
        try {
            if (dto == null) {
                throw new IllegalArgumentException("Les données du participant sont obligatoires");
            }

            // Validation des champs obligatoires
            if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom du participant est obligatoire");
            }
            if (dto.getPrenom() == null || dto.getPrenom().trim().isEmpty()) {
                throw new IllegalArgumentException("Le prénom du participant est obligatoire");
            }
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("L'email du participant est obligatoire");
            }

            Participant participant = (dto.getId() != null)
                    ? repo.findById(dto.getId()).orElse(new Participant())
                    : new Participant();

            participant.setNom(dto.getNom().trim());
            participant.setPrenom(dto.getPrenom().trim());
            participant.setEmail(dto.getEmail().trim());

            // Correction ici : Tel est un Long → pas de trim()
            participant.setTel(dto.getTel());   // On garde tel tel quel (pas de trim)

            // Association Structure (optionnelle)
            if (dto.getStructureId() != null) {
                participant.setStructure(structureRepo.findById(dto.getStructureId())
                        .orElseThrow(() -> new IllegalArgumentException("Structure non trouvée avec l'ID : " + dto.getStructureId())));
            } else {
                participant.setStructure(null);
            }

            // Association Profil (optionnelle)
            if (dto.getProfilId() != null) {
                participant.setProfil(profilRepo.findById(dto.getProfilId())
                        .orElseThrow(() -> new IllegalArgumentException("Profil non trouvé avec l'ID : " + dto.getProfilId())));
            } else {
                participant.setProfil(null);
            }

            Participant saved = repo.save(participant);
            return toDTO(saved);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du participant : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ParticipantDTO update(Long id, ParticipantDTO dto) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la mise à jour");
            }
            dto.setId(id);
            return save(dto);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour du participant : " + e.getMessage(), e);
        }
    }

    @Override
    public List<ParticipantDTO> findAll() {
        try {
            return repo.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des participants : " + e.getMessage(), e);
        }
    }

    @Override
    public ParticipantDTO findById(Long id) {
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
            throw new RuntimeException("Erreur lors de la recherche du participant : " + e.getMessage(), e);
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
                throw new IllegalArgumentException("Participant avec l'ID " + id + " non trouvé");
            }

            repo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du participant : " + e.getMessage(), e);
        }
    }

    private ParticipantDTO toDTO(Participant p) {
        if (p == null) return null;

        ParticipantDTO dto = new ParticipantDTO();
        dto.setId(p.getId());
        dto.setNom(p.getNom());
        dto.setPrenom(p.getPrenom());
        dto.setEmail(p.getEmail());
        dto.setTel(p.getTel());                    // Tel reste en Long

        if (p.getStructure() != null) {
            dto.setStructureId(p.getStructure().getId());
            dto.setStructureLibelle(p.getStructure().getLibelle());
        }
        if (p.getProfil() != null) {
            dto.setProfilId(p.getProfil().getId());
            dto.setProfilLibelle(p.getProfil().getLibelle());
        }
        return dto;
    }
}