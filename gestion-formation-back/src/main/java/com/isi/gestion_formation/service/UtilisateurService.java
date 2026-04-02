package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.UtilisateurDTO;
import com.isi.gestion_formation.model.Utilisateur;
import com.isi.gestion_formation.repository.iRepository.IUtilisateurRepository;
import com.isi.gestion_formation.repository.iRepository.IRoleRepository;
import com.isi.gestion_formation.service.iService.IUtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService implements IUtilisateurService {

    private final IUtilisateurRepository repo;
    private final IRoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Override
    public UtilisateurDTO login(String login, String password) {
        // 1. Chercher l'utilisateur par login
        Utilisateur u = repo.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Vérifier le mot de passe crypté (clair vs crypté)
        if (encoder.matches(password, u.getPassword())) {
            return toDTO(u);
        } else {
            throw new RuntimeException("Mot de passe incorrect");
        }
    }

    @Override
    public UtilisateurDTO save(UtilisateurDTO dto) {
        Utilisateur u = (dto.getId() != null) ?
                repo.findById(dto.getId()).orElse(new Utilisateur()) : new Utilisateur();

        u.setLogin(dto.getLogin());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPassword(encoder.encode(dto.getPassword()));   // BCrypt
        }
        if (dto.getRoleId() != null) {
            u.setRole(roleRepo.findById(dto.getRoleId()).orElse(null));
        }

        Utilisateur saved = repo.save(u);
        return toDTO(saved);
    }

    @Override
    public UtilisateurDTO update(Long id, UtilisateurDTO dto) {
        dto.setId(id);
        return save(dto);
    }

    @Override
    public List<UtilisateurDTO> findAll() {
        return repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurDTO findById(Long id) {
        return repo.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private UtilisateurDTO toDTO(Utilisateur u) {
        return new UtilisateurDTO(
                u.getId(),
                u.getLogin(),
                null,                                 // on ne renvoie jamais le mot de passe
                u.getRole() != null ? u.getRole().getId() : null,
                u.getRole() != null ? u.getRole().getNom().name() : null
        );
    }
}