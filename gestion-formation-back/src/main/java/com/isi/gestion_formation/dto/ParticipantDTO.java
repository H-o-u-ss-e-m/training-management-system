package com.isi.gestion_formation.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ParticipantDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Long tel;
    private Long structureId;
    private String structureLibelle;
    private Long profilId;
    private String profilLibelle;
}