package com.isi.gestion_formation.controller;

import com.isi.gestion_formation.dto.UtilisateurDTO;
import com.isi.gestion_formation.service.iService.IUtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@CrossOrigin( origins = "http://localhost:4200")
public class UtilisateurController {

    private final IUtilisateurService service;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UtilisateurDTO loginRequest) {
        try {
            UtilisateurDTO user = service.login(loginRequest.getLogin(), loginRequest.getPassword());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    @PostMapping("/save")
    public ResponseEntity<UtilisateurDTO> save(@Valid @RequestBody UtilisateurDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/update/{id}")                     // ← UPDATE ajouté
    public ResponseEntity<UtilisateurDTO> update(@PathVariable Long id, @Valid @RequestBody UtilisateurDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/all")
    public List<UtilisateurDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> getById(@PathVariable Long id) {
        UtilisateurDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}