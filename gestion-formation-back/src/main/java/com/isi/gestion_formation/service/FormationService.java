package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.FormationDTO;
import com.isi.gestion_formation.model.Formation;
import com.isi.gestion_formation.model.Participant;
import com.isi.gestion_formation.repository.iRepository.*;
import com.isi.gestion_formation.service.iService.IFormationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        Formation f = (dto.getId() != null) ?
                formationRepo.findById(dto.getId()).orElse(new Formation()) : new Formation();

        f.setTitre(dto.getTitre());
        f.setAnnee(dto.getAnnee());
        f.setDuree(dto.getDuree());
        f.setBudget(dto.getBudget());
        f.setLieu(dto.getLieu());
        f.setDateFormation(dto.getDateFormation());

        if (dto.getDomaineId() != null) {
            f.setDomaine(domaineRepo.findById(dto.getDomaineId())
                    .orElseThrow(() -> new RuntimeException("Domaine non trouvé")));
        }
        if (dto.getFormateurId() != null) {
            f.setFormateur(formateurRepo.findById(dto.getFormateurId())
                    .orElseThrow(() -> new RuntimeException("Formateur non trouvé")));
        }

        // Association sécurisée
        if (dto.getParticipantIds() != null && !dto.getParticipantIds().isEmpty()) {
            List<Participant> participants = participantRepo.findAllById(dto.getParticipantIds());
            f.setParticipants(participants);

            participants.forEach(p -> {
                if (!p.getFormations().contains(f)) {
                    p.getFormations().add(f);
                }
            });
        } else {
            f.setParticipants(new ArrayList<>()); // pas de participants
        }

        Formation saved = formationRepo.save(f);
        return toDTO(saved);
    }

    @Override
    public FormationDTO update(Long id, FormationDTO dto) {
        dto.setId(id);
        return save(dto);
    }

    @Override
    public List<FormationDTO> findAll() {
        return formationRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public FormationDTO findById(Long id) {
        return formationRepo.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public void delete(Long id) {
        formationRepo.deleteById(id);
    }

    private FormationDTO toDTO(Formation f) {
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
        if (f.getFormateur() != null) dto.setFormateurId(f.getFormateur().getId());

        if (f.getParticipants() != null) {
            dto.setParticipantIds(f.getParticipants().stream()
                    .map(Participant::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}