package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employeurs")
@CrossOrigin( origins = "http://localhost:4200")
public class EmployeurController {

    private final ISimpleValueService service;

    public EmployeurController(@Qualifier("employeurService") ISimpleValueService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<SimpleValueDTO> save(@Valid @RequestBody SimpleValueDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SimpleValueDTO> update(@PathVariable Long id, @Valid @RequestBody SimpleValueDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/all")
    public List<SimpleValueDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleValueDTO> getById(@PathVariable Long id) {
        SimpleValueDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}