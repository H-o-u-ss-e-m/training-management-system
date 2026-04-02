package com.isi.gestion_formation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "formateur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Formateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Email invalide")
    private String email;

    private String tel;

    public enum TypeFormateur {
        INTERNE, EXTERNE
    }

    @Enumerated(EnumType.STRING)
    private TypeFormateur type;

    @ManyToOne
    @JoinColumn(name = "id_employeur")
    private Employeur employeur;
}