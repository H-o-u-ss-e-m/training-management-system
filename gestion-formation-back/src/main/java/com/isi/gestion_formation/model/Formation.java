package com.isi.gestion_formation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "formation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Column(nullable = false, length = 200)
    private String titre;

    @NotNull(message = "L'année est obligatoire")
    private Integer annee;

    @NotNull(message = "La durée est obligatoire")
    @Min(1)
    private Integer duree;

    private Double budget;

    private String lieu;

    @Column(name = "date_formation")
    private LocalDate dateFormation;

    @ManyToOne
    @JoinColumn(name = "id_domaine", nullable = false)
    private Domaine domaine;

    @ManyToOne
    @JoinColumn(name = "id_formateur")
    private Formateur formateur;

    @ManyToMany(mappedBy = "formations")
    private List<Participant> participants = new ArrayList<>();   // ← INITIALISATION OBLIGATOIRE
}