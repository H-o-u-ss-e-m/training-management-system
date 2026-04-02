package com.isi.gestion_formation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "role")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;





    public enum NomRole {
        SIMPLE_UTILISATEUR,
        RESPONSABLE,
        ADMINISTRATEUR
    }


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NomRole nom;
}