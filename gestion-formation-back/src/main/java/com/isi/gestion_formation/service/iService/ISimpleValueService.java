package com.isi.gestion_formation.service.iService;

import com.isi.gestion_formation.dto.SimpleValueDTO;
import java.util.List;

public interface ISimpleValueService {
    SimpleValueDTO save(SimpleValueDTO dto);
    SimpleValueDTO update(Long id, SimpleValueDTO dto);
    List<SimpleValueDTO> findAll();
    SimpleValueDTO findById(Long id);
    void delete(Long id);
}