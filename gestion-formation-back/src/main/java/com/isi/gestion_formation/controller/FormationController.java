package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.FormationDTO;
import com.isi.gestion_formation.service.iService.IFormationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
@CrossOrigin( origins = "http://localhost:4200")
public class FormationController {

    private final IFormationService service;

    @PostMapping("/save")
    public ResponseEntity<FormationDTO> save(@Valid @RequestBody FormationDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/update/{id}")                     // ← UPDATE ajouté
    public ResponseEntity<FormationDTO> update(@PathVariable Long id, @Valid @RequestBody FormationDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/all")
    public List<FormationDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<FormationDTO> getById(@PathVariable Long id) {
        FormationDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}