package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.StatDTO;
import com.isi.gestion_formation.service.iService.IStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class StatistiqueController {

    private final IStatistiqueService service;

    @GetMapping("/par-annee")
    public ResponseEntity<List<StatDTO>> parAnnee() {
        try {
            List<StatDTO> stats = service.formationsParAnnee();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/par-domaine")
    public ResponseEntity<List<StatDTO>> parDomaine() {
        try {
            List<StatDTO> stats = service.formationsParDomaine();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/par-structure")
    public ResponseEntity<List<StatDTO>> parStructure() {
        try {
            List<StatDTO> stats = service.formationsParStructure();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ AJOUTER CE NOUVEL ENDPOINT
    @GetMapping("/par-mois")
    public ResponseEntity<List<StatDTO>> parMois() {
        try {
            List<StatDTO> stats = service.formationsParMois();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/totaux")
    public ResponseEntity<Map<String, Long>> totaux() {
        try {
            Map<String, Long> totaux = Map.of(
                    "formations", service.totalFormations(),
                    "participants", service.totalParticipants(),
                    "formateurs", service.totalFormateurs()
            );
            return ResponseEntity.ok(totaux);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}