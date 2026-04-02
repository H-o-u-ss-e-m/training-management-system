package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Profil;
import com.isi.gestion_formation.repository.iRepository.IProfilRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service("profilService")
@RequiredArgsConstructor
public class ProfilService implements ISimpleValueService {

    private final IProfilRepository repo;

    @Override
    public SimpleValueDTO save(SimpleValueDTO dto) {
        Profil p = (dto.getId() != null) ? repo.findById(dto.getId()).orElse(new Profil()) : new Profil();
        p.setLibelle(dto.getLibelle());
        Profil saved = repo.save(p);
        return new SimpleValueDTO(saved.getId(), saved.getLibelle());
    }

    @Override
    public SimpleValueDTO update(Long id, SimpleValueDTO dto) { dto.setId(id); return save(dto); }

    @Override
    public List<SimpleValueDTO> findAll() {
        return repo.findAll().stream()
                .map(p -> new SimpleValueDTO(p.getId(), p.getLibelle()))
                .collect(Collectors.toList());
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        return repo.findById(id).map(p -> new SimpleValueDTO(p.getId(), p.getLibelle())).orElse(null);
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }
}