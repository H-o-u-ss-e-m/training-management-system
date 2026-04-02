package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.StatDTO;
import com.isi.gestion_formation.repository.iRepository.IFormationRepository;
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

    private final IFormationRepository formationRepo;
    private final IParticipantRepository participantRepo;
    private final IFormateurRepository formateurRepo;

    @Override
    public List<StatDTO> formationsParAnnee() {
        return formationRepo.countByAnnee().stream()
                .map(row -> new StatDTO(String.valueOf(row[0]), (Long) row[1]))
                .collect(Collectors.toList());
    }

    @Override
    public List<StatDTO> formationsParDomaine() {
        return formationRepo.countByDomaine().stream()
                .map(row -> new StatDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    @Override
    public List<StatDTO> formationsParStructure() {
        return formationRepo.countByStructure().stream()
                .map(row -> new StatDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    @Override
    public Long totalFormations() { return formationRepo.count(); }

    @Override
    public Long totalParticipants() { return participantRepo.count(); }

    @Override
    public Long totalFormateurs() { return formateurRepo.count(); }
}