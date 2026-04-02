package com.isi.gestion_formation.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UtilisateurRequestDTO {
    private String login;
    private String password;
    private Long roleId; // On envoie juste l'ID du rôle
}