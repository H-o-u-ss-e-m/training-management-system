package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.FormateurDTO;
import com.isi.gestion_formation.service.iService.IFormateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/formateurs")
@RequiredArgsConstructor
@CrossOrigin( origins = "http://localhost:4200")
public class FormateurController {
    private final IFormateurService service;

    @PostMapping("/save")
    public ResponseEntity<FormateurDTO> save(@Valid @RequestBody FormateurDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<FormateurDTO> update(@PathVariable Long id, @Valid @RequestBody FormateurDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/all")
    public List<FormateurDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<FormateurDTO> getById(@PathVariable Long id) {
        FormateurDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}