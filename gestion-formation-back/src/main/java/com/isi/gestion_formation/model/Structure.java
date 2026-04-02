package com.isi.gestion_formation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "structure")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Structure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le libellé de la structure est obligatoire")
    @Column(nullable = false, length = 150)
    private String libelle;
}