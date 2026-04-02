package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Domaine;
import com.isi.gestion_formation.repository.iRepository.IDomaineRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service("domaineService")
@RequiredArgsConstructor
public class DomaineService implements ISimpleValueService {

    private final IDomaineRepository repo;

    @Override
    public SimpleValueDTO save(SimpleValueDTO dto) {
        Domaine d = (dto.getId() != null) ? repo.findById(dto.getId()).orElse(new Domaine()) : new Domaine();
        d.setLibelle(dto.getLibelle());
        Domaine saved = repo.save(d);
        return new SimpleValueDTO(saved.getId(), saved.getLibelle());
    }

    @Override
    public SimpleValueDTO update(Long id, SimpleValueDTO dto) {
        dto.setId(id);
        return save(dto);
    }

    @Override
    public List<SimpleValueDTO> findAll() {
        return repo.findAll().stream()
                .map(d -> new SimpleValueDTO(d.getId(), d.getLibelle()))
                .collect(Collectors.toList());
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        return repo.findById(id)
                .map(d -> new SimpleValueDTO(d.getId(), d.getLibelle()))
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}