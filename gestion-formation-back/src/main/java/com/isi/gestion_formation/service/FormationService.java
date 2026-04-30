package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.FormationDTO;
import com.isi.gestion_formation.model.Formation;
import com.isi.gestion_formation.model.Participant;
import com.isi.gestion_formation.model.Formateur;
import com.isi.gestion_formation.repository.iRepository.*;
import com.isi.gestion_formation.service.iService.IFormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormationService implements IFormationService {

    private final IFormationRepository formationRepo;
    private final IDomaineRepository domaineRepo;
    private final IFormateurRepository formateurRepo;
    private final IParticipantRepository participantRepo;
    private final EmailService emailService;

    @Override
    @Transactional
    public FormationDTO save(FormationDTO dto) {
        try {
            if (dto == null) throw new IllegalArgumentException("Les données de la formation sont obligatoires");
            if (dto.getTitre() == null || dto.getTitre().trim().isEmpty())
                throw new IllegalArgumentException("Le titre de la formation est obligatoire");
            if (dto.getAnnee() == null)
                throw new IllegalArgumentException("L'année de la formation est obligatoire");

            boolean isNew = (dto.getId() == null);

            Formation before = null;
            if (!isNew) {
                before = formationRepo.findById(dto.getId()).orElse(null);
            }

            Formation formation = isNew ? new Formation() : formationRepo.findById(dto.getId()).orElse(new Formation());

            formation.setTitre(dto.getTitre().trim());
            formation.setAnnee(dto.getAnnee());
            formation.setDuree(dto.getDuree());
            formation.setBudget(dto.getBudget());
            formation.setLieu(dto.getLieu() != null ? dto.getLieu().trim() : null);
            formation.setDateFormation(dto.getDateFormation());

            // Domaine
            if (dto.getDomaineId() != null) {
                formation.setDomaine(domaineRepo.findById(dto.getDomaineId())
                        .orElseThrow(() -> new IllegalArgumentException("Domaine non trouvé avec l'ID : " + dto.getDomaineId())));
            } else {
                formation.setDomaine(null);
            }

            // Formateur
            Formateur newFormateur = null;
            if (dto.getFormateurId() != null) {
                newFormateur = formateurRepo.findById(dto.getFormateurId())
                        .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé avec l'ID : " + dto.getFormateurId()));
                formation.setFormateur(newFormateur);
            } else {
                formation.setFormateur(null);
            }

            // Participants
            List<Participant> newParticipants = new ArrayList<>();
            if (dto.getParticipantIds() != null && !dto.getParticipantIds().isEmpty()) {
                newParticipants = participantRepo.findAllById(dto.getParticipantIds());
                if (newParticipants.size() != dto.getParticipantIds().size()) {
                    throw new IllegalArgumentException("Un ou plusieurs participants n'ont pas été trouvés");
                }
                formation.setParticipants(newParticipants);

                // relation bidirectionnelle
                newParticipants.forEach(p -> {
                    if (!p.getFormations().contains(formation)) {
                        p.getFormations().add(formation);
                    }
                });
            } else {
                formation.setParticipants(new ArrayList<>());
            }

            Formation saved = formationRepo.save(formation);

            // -----------------------
            // EMAILS après sauvegarde
            // -----------------------
            String dateStr = saved.getDateFormation() != null ? saved.getDateFormation().toString() : null;
            String domaineLibelle = saved.getDomaine() != null ? saved.getDomaine().getLibelle() : null;

            // 1) Formateur : envoyer si création ou formateur changé
            if (saved.getFormateur() != null) {
                boolean formateurChanged = (before == null)
                        || (before.getFormateur() == null)
                        || (!Objects.equals(before.getFormateur().getId(), saved.getFormateur().getId()));

                if (formateurChanged) {
                    emailService.sendFormateurAssignationEmail(
                            saved.getFormateur().getEmail(),
                            saved.getFormateur().getPrenom() + " " + saved.getFormateur().getNom(),
                            saved.getTitre(),
                            saved.getLieu(),
                            dateStr,
                            saved.getDuree(),
                            saved.getBudget(),
                            saved.getParticipants() != null ? saved.getParticipants().size() : 0
                    );
                }
            }

            // 2) Participants : envoyer seulement aux nouveaux ajoutés (ou tous si création)
            Set<Long> beforeParticipantIds = new HashSet<>();
            if (before != null && before.getParticipants() != null) {
                beforeParticipantIds = before.getParticipants().stream()
                        .map(Participant::getId)
                        .collect(Collectors.toSet());
            }

            if (saved.getParticipants() != null) {
                for (Participant p : saved.getParticipants()) {
                    boolean isNewParticipantInThisFormation = (before == null) || !beforeParticipantIds.contains(p.getId());
                    if (isNewParticipantInThisFormation) {
                        emailService.sendInscriptionEmail(
                                p.getEmail(),
                                p.getPrenom() + " " + p.getNom(),
                                saved.getTitre(),
                                saved.getLieu(),
                                dateStr,
                                saved.getDuree()
                        );
                    }
                }
            }

            return toDTO(saved);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de la formation : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public FormationDTO update(Long id, FormationDTO dto) {
        if (id == null) throw new IllegalArgumentException("L'ID est obligatoire pour la mise à jour");
        dto.setId(id);
        return save(dto);
    }

    @Override
    public List<FormationDTO> findAll() {
        return formationRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public FormationDTO findById(Long id) {
        if (id == null) throw new IllegalArgumentException("L'ID est obligatoire");
        return formationRepo.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("L'ID est obligatoire pour la suppression");
        if (!formationRepo.existsById(id)) throw new IllegalArgumentException("Formation avec l'ID " + id + " non trouvée");
        formationRepo.deleteById(id);
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
            dto.setParticipantIds(f.getParticipants().stream().map(Participant::getId).collect(Collectors.toList()));
        }
        return dto;
    }
}