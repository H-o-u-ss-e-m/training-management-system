package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.ParticipantDTO;
import com.isi.gestion_formation.service.iService.IParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
@CrossOrigin( origins = "http://localhost:4200")
public class ParticipantController {

    private final IParticipantService service;

    @PostMapping("/save")
    public ResponseEntity<ParticipantDTO> save(@Valid @RequestBody ParticipantDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/update/{id}")                     // ← UPDATE ajouté
    public ResponseEntity<ParticipantDTO> update(@PathVariable Long id, @Valid @RequestBody ParticipantDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/all")
    public List<ParticipantDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDTO> getById(@PathVariable Long id) {
        ParticipantDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}