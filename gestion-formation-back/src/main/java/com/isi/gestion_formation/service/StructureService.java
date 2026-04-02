package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Structure;
import com.isi.gestion_formation.repository.iRepository.IStructureRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service("structureService")
@RequiredArgsConstructor
public class StructureService implements ISimpleValueService {

    private final IStructureRepository repo;

    @Override
    public SimpleValueDTO save(SimpleValueDTO dto) {
        Structure s = (dto.getId() != null) ? repo.findById(dto.getId()).orElse(new Structure()) : new Structure();
        s.setLibelle(dto.getLibelle());
        Structure saved = repo.save(s);
        return new SimpleValueDTO(saved.getId(), saved.getLibelle());
    }

    @Override
    public SimpleValueDTO update(Long id, SimpleValueDTO dto) { dto.setId(id); return save(dto); }

    @Override
    public List<SimpleValueDTO> findAll() {
        return repo.findAll().stream()
                .map(s -> new SimpleValueDTO(s.getId(), s.getLibelle()))
                .collect(Collectors.toList());
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        return repo.findById(id).map(s -> new SimpleValueDTO(s.getId(), s.getLibelle())).orElse(null);
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }
}