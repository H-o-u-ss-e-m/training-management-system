package com.isi.gestion_formation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participant")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Email invalide")
    private String email;

    private Long tel;

    @ManyToOne
    @JoinColumn(name = "id_structure", nullable = false)
    private Structure structure;

    @ManyToOne
    @JoinColumn(name = "id_profil", nullable = false)
    private Profil profil;

    @ManyToMany
    @JoinTable(
            name = "participer",
            joinColumns = @JoinColumn(name = "id_participant"),
            inverseJoinColumns = @JoinColumn(name = "id_formation")
    )
    private List<Formation> formations = new ArrayList<>();   // ← INITIALISATION OBLIGATOIRE
}