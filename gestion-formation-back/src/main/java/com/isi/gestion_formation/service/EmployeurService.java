package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Employeur;
import com.isi.gestion_formation.repository.iRepository.IEmployeurRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("employeurService")
@RequiredArgsConstructor
public class EmployeurService implements ISimpleValueService {

    private final IEmployeurRepository repo;

    @Override
    @Transactional
    public SimpleValueDTO save(SimpleValueDTO dto) {
        try {
            // Validation basique
            if (dto == null || dto.getLibelle() == null || dto.getLibelle().trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de l'employeur est obligatoire");
            }

            Employeur employeur = (dto.getId() != null)
                    ? repo.findById(dto.getId()).orElse(new Employeur())
                    : new Employeur();

            employeur.setNomEmployeur(dto.getLibelle().trim());

            Employeur saved = repo.save(employeur);

            return new SimpleValueDTO(saved.getId(), saved.getNomEmployeur());

        } catch (IllegalArgumentException e) {
            throw e;   // Exception métier → sera traitée comme Bad Request
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de l'employeur : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SimpleValueDTO update(Long id, SimpleValueDTO dto) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la mise à jour");
            }
            dto.setId(id);
            return save(dto);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'employeur : " + e.getMessage(), e);
        }
    }

    @Override
    public List<SimpleValueDTO> findAll() {
        try {
            return repo.findAll().stream()
                    .map(e -> new SimpleValueDTO(e.getId(), e.getNomEmployeur()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des employeurs : " + e.getMessage(), e);
        }
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire");
            }

            return repo.findById(id)
                    .map(e -> new SimpleValueDTO(e.getId(), e.getNomEmployeur()))
                    .orElse(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de l'employeur : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la suppression");
            }

            if (!repo.existsById(id)) {
                throw new IllegalArgumentException("Employeur avec l'ID " + id + " non trouvé");
            }

            repo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de l'employeur : " + e.getMessage(), e);
        }
    }
}