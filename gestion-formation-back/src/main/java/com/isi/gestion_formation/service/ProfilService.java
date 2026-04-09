package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Profil;
import com.isi.gestion_formation.repository.iRepository.IProfilRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("profilService")
@RequiredArgsConstructor
public class ProfilService implements ISimpleValueService {

    private final IProfilRepository repo;

    @Override
    @Transactional
    public SimpleValueDTO save(SimpleValueDTO dto) {
        try {
            // Validation du libellé
            if (dto == null || dto.getLibelle() == null || dto.getLibelle().trim().isEmpty()) {
                throw new IllegalArgumentException("Le libellé du profil est obligatoire");
            }

            Profil profil = (dto.getId() != null)
                    ? repo.findById(dto.getId()).orElse(new Profil())
                    : new Profil();

            profil.setLibelle(dto.getLibelle().trim());

            Profil saved = repo.save(profil);
            return new SimpleValueDTO(saved.getId(), saved.getLibelle());

        } catch (IllegalArgumentException e) {
            throw e;                    // Erreur métier (400)
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du profil : " + e.getMessage(), e);
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
            throw new RuntimeException("Erreur lors de la mise à jour du profil : " + e.getMessage(), e);
        }
    }

    @Override
    public List<SimpleValueDTO> findAll() {
        try {
            return repo.findAll().stream()
                    .map(p -> new SimpleValueDTO(p.getId(), p.getLibelle()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des profils : " + e.getMessage(), e);
        }
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire");
            }

            return repo.findById(id)
                    .map(p -> new SimpleValueDTO(p.getId(), p.getLibelle()))
                    .orElse(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du profil : " + e.getMessage(), e);
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
                throw new IllegalArgumentException("Profil avec l'ID " + id + " non trouvé");
            }

            repo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du profil : " + e.getMessage(), e);
        }
    }
}