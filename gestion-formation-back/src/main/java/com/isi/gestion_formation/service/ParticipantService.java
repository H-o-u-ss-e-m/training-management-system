package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.ParticipantDTO;
import com.isi.gestion_formation.model.Participant;
import com.isi.gestion_formation.repository.iRepository.IParticipantRepository;
import com.isi.gestion_formation.repository.iRepository.IProfilRepository;
import com.isi.gestion_formation.repository.iRepository.IStructureRepository;
import com.isi.gestion_formation.service.iService.IParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService implements IParticipantService {

    private final IParticipantRepository repo;
    private final IStructureRepository structureRepo;
    private final IProfilRepository profilRepo;

    @Override
    public ParticipantDTO save(ParticipantDTO dto) {
        Participant p = (dto.getId() != null) ?
                repo.findById(dto.getId()).orElse(new Participant()) : new Participant();

        p.setNom(dto.getNom());
        p.setPrenom(dto.getPrenom());
        p.setEmail(dto.getEmail());
        p.setTel(dto.getTel());

        if (dto.getStructureId() != null)
            p.setStructure(structureRepo.findById(dto.getStructureId()).orElse(null));
        if (dto.getProfilId() != null)
            p.setProfil(profilRepo.findById(dto.getProfilId()).orElse(null));

        Participant saved = repo.save(p);
        return toDTO(saved);
    }

    @Override
    public ParticipantDTO update(Long id, ParticipantDTO dto) {
        dto.setId(id);
        return save(dto);
    }

    @Override
    public List<ParticipantDTO> findAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ParticipantDTO findById(Long id) {
        return repo.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private ParticipantDTO toDTO(Participant p) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setId(p.getId());
        dto.setNom(p.getNom());
        dto.setPrenom(p.getPrenom());
        dto.setEmail(p.getEmail());
        dto.setTel(p.getTel());
        if (p.getStructure() != null) {
            dto.setStructureId(p.getStructure().getId());
            dto.setStructureLibelle(p.getStructure().getLibelle());
        }
        if (p.getProfil() != null) {
            dto.setProfilId(p.getProfil().getId());
            dto.setProfilLibelle(p.getProfil().getLibelle());
        }
        return dto;
    }
}