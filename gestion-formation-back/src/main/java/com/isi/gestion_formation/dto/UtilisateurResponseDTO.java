package com.isi.gestion_formation.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UtilisateurResponseDTO {
    private Long id;
    private String login;
    private String roleNom; // On affiche le nom du rôle (ADMIN, etc.)
}