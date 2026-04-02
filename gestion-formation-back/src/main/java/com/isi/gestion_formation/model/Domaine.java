package com.isi.gestion_formation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "domaine")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Domaine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le libellé du domaine est obligatoire")
    @Column(nullable = false, length = 100)
    private String libelle;
}