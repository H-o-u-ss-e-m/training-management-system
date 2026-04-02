package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.StatDTO;
import com.isi.gestion_formation.service.iService.IStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
@CrossOrigin( origins = "http://localhost:4200")
public class StatistiqueController {
    private final IStatistiqueService service;

    @GetMapping("/par-annee")
    public List<StatDTO> parAnnee() { return service.formationsParAnnee(); }

    @GetMapping("/par-domaine")
    public List<StatDTO> parDomaine() { return service.formationsParDomaine(); }

    @GetMapping("/par-structure")
    public List<StatDTO> parStructure() { return service.formationsParStructure(); }

    @GetMapping("/totaux")
    public Map<String, Long> totaux() {
        return Map.of(
                "formations", service.totalFormations(),
                "participants", service.totalParticipants(),
                "formateurs", service.totalFormateurs()
        );
    }
}