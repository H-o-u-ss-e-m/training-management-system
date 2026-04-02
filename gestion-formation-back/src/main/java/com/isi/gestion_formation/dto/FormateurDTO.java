package com.isi.gestion_formation.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class FormateurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private String type;
    private Long employeurId;
    private String employeurNom;
}