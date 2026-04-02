package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import com.isi.gestion_formation.model.Employeur;
import com.isi.gestion_formation.repository.iRepository.IEmployeurRepository;
import com.isi.gestion_formation.service.iService.ISimpleValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service("employeurService")
@RequiredArgsConstructor
public class EmployeurService implements ISimpleValueService {

    private final IEmployeurRepository repo;

    @Override
    public SimpleValueDTO save(SimpleValueDTO dto) {
        Employeur e = (dto.getId() != null) ? repo.findById(dto.getId()).orElse(new Employeur()) : new Employeur();
        e.setNomEmployeur(dto.getLibelle());
        Employeur saved = repo.save(e);
        return new SimpleValueDTO(saved.getId(), saved.getNomEmployeur());
    }

    @Override
    public SimpleValueDTO update(Long id, SimpleValueDTO dto) { dto.setId(id); return save(dto); }

    @Override
    public List<SimpleValueDTO> findAll() {
        return repo.findAll().stream()
                .map(e -> new SimpleValueDTO(e.getId(), e.getNomEmployeur()))
                .collect(Collectors.toList());
    }

    @Override
    public SimpleValueDTO findById(Long id) {
        return repo.findById(id).map(e -> new SimpleValueDTO(e.getId(), e.getNomEmployeur())).orElse(null);
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }
}