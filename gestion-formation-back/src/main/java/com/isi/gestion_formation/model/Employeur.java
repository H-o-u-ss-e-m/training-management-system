package com.isi.gestion_formation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "employeur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Employeur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'employeur est obligatoire")
    @Column(name = "nom_employeur", nullable = false, length = 150)
    private String nomEmployeur;
}