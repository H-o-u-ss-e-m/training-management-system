package com.isi.gestion_formation.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UtilisateurDTO {
    private Long id;
    private String login;
    private String password;
    private Long roleId;
    private String roleNom;
}