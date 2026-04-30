package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.FormationDTO;
import com.isi.gestion_formation.dto.PropositionRequest;
import com.isi.gestion_formation.model.Participant;
import com.isi.gestion_formation.repository.iRepository.IParticipantRepository;
import com.isi.gestion_formation.service.EmailService;
import com.isi.gestion_formation.service.iService.IFormationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class FormationController {

    private final IFormationService service;
    private final IParticipantRepository participantRepo;
    private final EmailService emailService;

    @PostMapping("/save")
    public ResponseEntity<FormationDTO> save(@Valid @RequestBody FormationDTO dto) {
        try {
            FormationDTO saved = service.save(dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<FormationDTO> update(@PathVariable Long id, @Valid @RequestBody FormationDTO dto) {
        try {
            FormationDTO updated = service.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<FormationDTO>> getAll() {
        try {
            List<FormationDTO> list = service.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormationDTO> getById(@PathVariable Long id) {
        try {
            FormationDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ------------------------------------------------------------
    // ✅ 1) PROPOSITION (offre) à une liste de participants
    // POST /api/formations/{id}/proposer  body: { "participantIds":[1,2] }
    // ------------------------------------------------------------
    @PostMapping("/{id}/proposer")
    public ResponseEntity<String> proposer(@PathVariable Long id, @RequestBody PropositionRequest req) {
        try {
            FormationDTO formation = service.findById(id);
            if (formation == null) return ResponseEntity.notFound().build();

            List<Long> ids = (req != null) ? req.getParticipantIds() : null;
            if (ids == null || ids.isEmpty()) return ResponseEntity.badRequest().body("participantIds vide");

            List<Participant> participants = participantRepo.findAllById(ids);

            String dateStr = formation.getDateFormation() != null ? formation.getDateFormation().toString() : null;

            for (Participant p : participants) {
                emailService.sendPropositionEmail(
                        p.getEmail(),
                        p.getPrenom() + " " + p.getNom(),
                        formation.getTitre(),
                        formation.getLieu(),
                        dateStr,
                        formation.getDuree(),
                        formation.getDomaineLibelle()
                );
            }

            return ResponseEntity.ok("Propositions envoyées: " + participants.size());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // ✅ 2) HISTORIQUE : envoyer un récap à un participant
    // POST /api/formations/{formationId}/historique/{participantId}
    // ------------------------------------------------------------
    @PostMapping("/{formationId}/historique/{participantId}")
    public ResponseEntity<String> historique(@PathVariable Long formationId, @PathVariable Long participantId) {
        try {
            FormationDTO formation = service.findById(formationId);
            if (formation == null) return ResponseEntity.notFound().build();

            Participant p = participantRepo.findById(participantId)
                    .orElseThrow(() -> new IllegalArgumentException("Participant non trouvé"));

            String dateStr = formation.getDateFormation() != null ? formation.getDateFormation().toString() : null;

            emailService.sendHistoriqueEmail(
                    p.getEmail(),
                    p.getPrenom() + " " + p.getNom(),
                    formation.getTitre(),
                    dateStr,
                    formation.getLieu(),
                    formation.getDomaineLibelle()
            );

            return ResponseEntity.ok("Historique envoyé à " + p.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // ✅ 3) CAMPAGNE : anciens => historique, nouveaux => offre
    // POST /api/formations/campagne/{formationId}
    // ------------------------------------------------------------
    @PostMapping("/campagne/{formationId}")
    public ResponseEntity<String> campagne(@PathVariable Long formationId) {
        try {
            FormationDTO formation = service.findById(formationId);
            if (formation == null) return ResponseEntity.notFound().build();

            String dateStr = formation.getDateFormation() != null ? formation.getDateFormation().toString() : null;

            List<Participant> anciens = participantRepo.findParticipantsAyantDejaParticipe();
            for (Participant p : anciens) {
                emailService.sendHistoriqueEmail(
                        p.getEmail(),
                        p.getPrenom() + " " + p.getNom(),
                        formation.getTitre(),
                        dateStr,
                        formation.getLieu(),
                        formation.getDomaineLibelle()
                );
            }

            List<Participant> nouveaux = participantRepo.findParticipantsJamaisParticipe();
            for (Participant p : nouveaux) {
                emailService.sendPropositionEmail(
                        p.getEmail(),
                        p.getPrenom() + " " + p.getNom(),
                        formation.getTitre(),
                        formation.getLieu(),
                        dateStr,
                        formation.getDuree(),
                        formation.getDomaineLibelle()
                );
            }

            return ResponseEntity.ok("Campagne envoyée: anciens=" + anciens.size() + ", nouveaux=" + nouveaux.size());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur: " + e.getMessage());
        }
    }
}