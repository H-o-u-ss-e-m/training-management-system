package com.isi.gestion_formation.service;

import com.isi.gestion_formation.dto.UtilisateurDTO;
import com.isi.gestion_formation.model.Utilisateur;
import com.isi.gestion_formation.repository.iRepository.IRoleRepository;
import com.isi.gestion_formation.repository.iRepository.IUtilisateurRepository;
import com.isi.gestion_formation.service.iService.IUtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        try {
            if (login == null || login.trim().isEmpty()) {
                throw new IllegalArgumentException("Le login est obligatoire");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Le mot de passe est obligatoire");
            }

            // Recherche de l'utilisateur
            Utilisateur u = repo.findByLogin(login.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Login ou mot de passe incorrect"));

            // Vérification du mot de passe
            if (!encoder.matches(password, u.getPassword())) {
                throw new IllegalArgumentException("Login ou mot de passe incorrect");
            }

            return toDTO(u);

        } catch (IllegalArgumentException e) {
            throw e;   // On garde le message pour le controller (401)
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la connexion : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public UtilisateurDTO save(UtilisateurDTO dto) {
        try {
            if (dto == null) {
                throw new IllegalArgumentException("Les données de l'utilisateur sont obligatoires");
            }
            if (dto.getLogin() == null || dto.getLogin().trim().isEmpty()) {
                throw new IllegalArgumentException("Le login est obligatoire");
            }

            Utilisateur utilisateur = (dto.getId() != null)
                    ? repo.findById(dto.getId()).orElse(new Utilisateur())
                    : new Utilisateur();

            utilisateur.setLogin(dto.getLogin().trim());

            // Mise à jour du mot de passe uniquement s'il est fourni
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                utilisateur.setPassword(encoder.encode(dto.getPassword()));
            }

            // Association du rôle
            if (dto.getRoleId() != null) {
                utilisateur.setRole(roleRepo.findById(dto.getRoleId())
                        .orElseThrow(() -> new IllegalArgumentException("Rôle non trouvé avec l'ID : " + dto.getRoleId())));
            } else if (dto.getId() == null) {
                // Pour un nouvel utilisateur, un rôle est généralement obligatoire
                throw new IllegalArgumentException("Le rôle est obligatoire pour la création d'un utilisateur");
            }

            Utilisateur saved = repo.save(utilisateur);
            return toDTO(saved);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de l'utilisateur : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public UtilisateurDTO update(Long id, UtilisateurDTO dto) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire pour la mise à jour");
            }
            dto.setId(id);
            return save(dto);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage(), e);
        }
    }

    @Override
    public List<UtilisateurDTO> findAll() {
        try {
            return repo.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des utilisateurs : " + e.getMessage(), e);
        }
    }

    @Override
    public UtilisateurDTO findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("L'ID est obligatoire");
            }

            return repo.findById(id)
                    .map(this::toDTO)
                    .orElse(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur : " + e.getMessage(), e);
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
                throw new IllegalArgumentException("Utilisateur avec l'ID " + id + " non trouvé");
            }

            repo.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur : " + e.getMessage(), e);
        }
    }

    private UtilisateurDTO toDTO(Utilisateur u) {
        if (u == null) return null;

        return new UtilisateurDTO(
                u.getId(),
                u.getLogin(),
                null,                                   // Jamais renvoyer le mot de passe
                u.getRole() != null ? u.getRole().getId() : null,
                u.getRole() != null ? u.getRole().getNom().name() : null
        );
    }
}