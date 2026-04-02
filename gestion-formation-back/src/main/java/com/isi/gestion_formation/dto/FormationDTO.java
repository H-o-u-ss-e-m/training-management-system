package com.isi.gestion_formation.dto;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class FormationDTO {
    private Long id;
    private String titre;
    private Integer annee;
    private Integer duree;
    private Double budget;
    private String lieu;
    private LocalDate dateFormation;
    private Long domaineId;
    private String domaineLibelle;
    private Long formateurId;
    private List<Long> participantIds;
}