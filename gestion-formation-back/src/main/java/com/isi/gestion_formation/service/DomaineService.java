package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Domaine;
import com.isi.gestion_formation.repository.iRepository.IDomaineRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("domaineService")
@RequiredArgsConstructor
public class DomaineService implements ISimpleValueService {

    private final IDomaineRepository repo;

    @Override
    @Transactional
    public SimpleValueDTO save(SimpleValueDTO dto) {
        try {
            if (dto == null || dto.getLibelle() == null || dto.getLibelle().trim().isEmpty()) {
                throw new IllegalArgumentException("Le libellé est obligatoire");
            }

            Domaine domaine = (dto.getId() != null)
                    ? repo.findById(dto.getId()).orElse(new Domaine())
                    : new Domaine();

            domaine.setLibelle(dto.getLibelle().trim());

            Domaine saved = repo.save(domaine);
            return new SimpleValueDTO(saved.getId(), saved.getLibelle());

        } catch (IllegalArgumentException e) {
            throw e;                    // on laisse passer les exceptions métier
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du domaine : " + e.getMessage(), e);
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
            throw new RuntimeException("Erreur lors de la mise à jour du domaine : " + e.getMessage(), e);
        }
    }

    @Override
    public List<SimpleValueDTO> findAll() {
        try {
            return repo.findAll().stream()
                    .map(d -> new SimpleValueDTO(d.getId(), d.getLibelle()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des domaines : " + e.getMessage(), e);
        }
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire");
            }

            return repo.findById(id)
                    .map(d -> new SimpleValueDTO(d.getId(), d.getLibelle()))
                    .orElse(null);   // ou tu peux lancer une exception si tu préfères

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du domaine : " + e.getMessage(), e);
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
                throw new IllegalArgumentException("Domaine avec l'ID " + id + " non trouvé");
            }

            repo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du domaine : " + e.getMessage(), e);
        }
    }
}