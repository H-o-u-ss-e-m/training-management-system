package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.FormationDTO;
import com.isi.gestion_formation.model.Formation;
import com.isi.gestion_formation.model.Participant;
import com.isi.gestion_formation.repository.iRepository.*;
import com.isi.gestion_formation.service.iService.IFormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormationService implements IFormationService {

    private final IFormationRepository formationRepo;
    private final IDomaineRepository domaineRepo;
    private final IFormateurRepository formateurRepo;
    private final IParticipantRepository participantRepo;

    @Override
    @Transactional
    public FormationDTO save(FormationDTO dto) {
        try {
            if (dto == null) {
                throw new IllegalArgumentException("Les données de la formation sont obligatoires");
            }

            // Validation des champs obligatoires
            if (dto.getTitre() == null || dto.getTitre().trim().isEmpty()) {
                throw new IllegalArgumentException("Le titre de la formation est obligatoire");
            }
            if (dto.getAnnee() == null) {
                throw new IllegalArgumentException("L'année de la formation est obligatoire");
            }

            Formation formation = (dto.getId() != null)
                    ? formationRepo.findById(dto.getId()).orElse(new Formation())
                    : new Formation();

            formation.setTitre(dto.getTitre().trim());
            formation.setAnnee(dto.getAnnee());
            formation.setDuree(dto.getDuree());
            formation.setBudget(dto.getBudget());
            formation.setLieu(dto.getLieu() != null ? dto.getLieu().trim() : null);
            formation.setDateFormation(dto.getDateFormation());

            // Association Domaine
            if (dto.getDomaineId() != null) {
                formation.setDomaine(domaineRepo.findById(dto.getDomaineId())
                        .orElseThrow(() -> new IllegalArgumentException("Domaine non trouvé avec l'ID : " + dto.getDomaineId())));
            } else {
                formation.setDomaine(null);
            }

            // Association Formateur
            if (dto.getFormateurId() != null) {
                formation.setFormateur(formateurRepo.findById(dto.getFormateurId())
                        .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé avec l'ID : " + dto.getFormateurId())));
            } else {
                formation.setFormateur(null);
            }

            // Association Participants (relation bidirectionnelle)
            if (dto.getParticipantIds() != null && !dto.getParticipantIds().isEmpty()) {
                List<Participant> participants = participantRepo.findAllById(dto.getParticipantIds());

                // Vérifier que tous les participants existent
                if (participants.size() != dto.getParticipantIds().size()) {
                    throw new IllegalArgumentException("Un ou plusieurs participants n'ont pas été trouvés");
                }

                formation.setParticipants(participants);

                // Mise à jour de la relation bidirectionnelle
                participants.forEach(p -> {
                    if (!p.getFormations().contains(formation)) {
                        p.getFormations().add(formation);
                    }
                });
            } else {
                formation.setParticipants(new ArrayList<>());
            }

            Formation saved = formationRepo.save(formation);
            return toDTO(saved);

        } catch (IllegalArgumentException e) {
            throw e; // Erreur métier → sera traitée en 400 dans le controller
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de la formation : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FormationDTO update(Long id, FormationDTO dto) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la mise à jour");
            }
            dto.setId(id);
            return save(dto);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la formation : " + e.getMessage(), e);
        }
    }

    @Override
    public List<FormationDTO> findAll() {
        try {
            return formationRepo.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des formations : " + e.getMessage(), e);
        }
    }

    @Override
    public FormationDTO findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire");
            }

            return formationRepo.findById(id)
                    .map(this::toDTO)
                    .orElse(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de la formation : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la suppression");
            }

            if (!formationRepo.existsById(id)) {
                throw new IllegalArgumentException("Formation avec l'ID " + id + " non trouvée");
            }

            formationRepo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de la formation : " + e.getMessage(), e);
        }
    }

    private FormationDTO toDTO(Formation f) {
        if (f == null) return null;

        FormationDTO dto = new FormationDTO();
        dto.setId(f.getId());
        dto.setTitre(f.getTitre());
        dto.setAnnee(f.getAnnee());
        dto.setDuree(f.getDuree());
        dto.setBudget(f.getBudget());
        dto.setLieu(f.getLieu());
        dto.setDateFormation(f.getDateFormation());

        if (f.getDomaine() != null) {
            dto.setDomaineId(f.getDomaine().getId());
            dto.setDomaineLibelle(f.getDomaine().getLibelle());
        }
        if (f.getFormateur() != null) {
            dto.setFormateurId(f.getFormateur().getId());
        }

        if (f.getParticipants() != null) {
            dto.setParticipantIds(f.getParticipants().stream()
                    .map(Participant::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}