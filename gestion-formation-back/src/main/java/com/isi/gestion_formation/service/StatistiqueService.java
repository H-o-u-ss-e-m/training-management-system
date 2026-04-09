package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.StatDTO;
import com.isi.gestion_formation.repository.FormationRepository;
import com.isi.gestion_formation.repository.iRepository.IFormateurRepository;
import com.isi.gestion_formation.repository.iRepository.IParticipantRepository;
import com.isi.gestion_formation.service.iService.IStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatistiqueService implements IStatistiqueService {

    private final FormationRepository formationRepository;
    private final IParticipantRepository participantRepo;
    private final IFormateurRepository formateurRepo;

    @Override
    public List<StatDTO> formationsParAnnee() {
        try {
            return formationRepository.getFormationsParAnnee().stream()
                    .map(row -> new StatDTO(
                            String.valueOf(row[0]),
                            (Long) row[1]
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul des formations par année : " + e.getMessage(), e);
        }
    }

    @Override
    public List<StatDTO> formationsParDomaine() {
        try {
            return formationRepository.getFormationsParDomaine().stream()
                    .map(row -> new StatDTO(
                            (String) row[0],
                            (Long) row[1]
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul des formations par domaine : " + e.getMessage(), e);
        }
    }

    @Override
    public List<StatDTO> formationsParStructure() {
        try {
            return formationRepository.getFormationsParStructure().stream()
                    .map(row -> new StatDTO(
                            (String) row[0],
                            (Long) row[1]
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul des formations par structure : " + e.getMessage(), e);
        }
    }

    @Override
    public List<StatDTO> formationsParMois() {
        try {
            return formationRepository.getFormationsParMois().stream()
                    .map(row -> new StatDTO(
                            (String) row[0],
                            (Long) row[1]
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul des formations par mois : " + e.getMessage(), e);
        }
    }

    @Override
    public Long totalFormations() {
        try {
            return formationRepository.getTotalFormations();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du total des formations : " + e.getMessage(), e);
        }
    }

    @Override
    public Long totalParticipants() {
        try {
            return participantRepo.count();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du total des participants : " + e.getMessage(), e);
        }
    }

    @Override
    public Long totalFormateurs() {
        try {
            return formateurRepo.count();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du total des formateurs : " + e.getMessage(), e);
        }
    }
}