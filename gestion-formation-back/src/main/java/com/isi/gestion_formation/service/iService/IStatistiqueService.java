package com.isi.gestion_formation.service.iService;

import com.isi.gestion_formation.dto.StatDTO;
import java.util.List;

public interface IStatistiqueService {
    List<StatDTO> formationsParAnnee();
    List<StatDTO> formationsParDomaine();
    List<StatDTO> formationsParStructure();
    Long totalFormations();
    Long totalParticipants();
    Long totalFormateurs();
}