package tn.esprit.astba.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Attributs communs à tous les rôles
    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDate dateNaissance;

    private String numTelephone;

    @Column(nullable = false)
    private Boolean status = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Attributs spécifiques pour ELEVE
    private String numTelephoneParent;

    @Enumerated(EnumType.STRING)
    private niveauCapacite niveauCapacite;

    public enum Role {
        ADMIN,
        FORMATEUR,
        RESPONSABLE_FORMATION,
        ELEVE
    }

    public enum niveauCapacite {
        STANDARD,
        SUPPORT
    }
}