package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Structure;
import com.isi.gestion_formation.repository.iRepository.IStructureRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("structureService")
@RequiredArgsConstructor
public class StructureService implements ISimpleValueService {

    private final IStructureRepository repo;

    @Override
    @Transactional
    public SimpleValueDTO save(SimpleValueDTO dto) {
        try {
            // Validation du libellé
            if (dto == null || dto.getLibelle() == null || dto.getLibelle().trim().isEmpty()) {
                throw new IllegalArgumentException("Le libellé de la structure est obligatoire");
            }

            Structure structure = (dto.getId() != null)
                    ? repo.findById(dto.getId()).orElse(new Structure())
                    : new Structure();

            structure.setLibelle(dto.getLibelle().trim());

            Structure saved = repo.save(structure);
            return new SimpleValueDTO(saved.getId(), saved.getLibelle());

        } catch (IllegalArgumentException e) {
            throw e;                    // Erreur métier → 400 Bad Request
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de la structure : " + e.getMessage(), e);
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
            throw new RuntimeException("Erreur lors de la mise à jour de la structure : " + e.getMessage(), e);
        }
    }

    @Override
    public List<SimpleValueDTO> findAll() {
        try {
            return repo.findAll().stream()
                    .map(s -> new SimpleValueDTO(s.getId(), s.getLibelle()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des structures : " + e.getMessage(), e);
        }
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire");
            }

            return repo.findById(id)
                    .map(s -> new SimpleValueDTO(s.getId(), s.getLibelle()))
                    .orElse(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de la structure : " + e.getMessage(), e);
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
                throw new IllegalArgumentException("Structure avec l'ID " + id + " non trouvée");
            }

            repo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de la structure : " + e.getMessage(), e);
        }
    }
}