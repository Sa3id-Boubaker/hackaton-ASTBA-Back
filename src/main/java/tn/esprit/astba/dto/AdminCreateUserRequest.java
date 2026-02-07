package tn.esprit.astba.dto;

import lombok.Data;
import tn.esprit.astba.entity.User;

import java.time.LocalDate;

@Data
public class AdminCreateUserRequest {
    private String nom;
    private String prenom;
    private String email;
    private LocalDate dateNaissance;
    private String numTelephone;
    private User.Role role; // ELEVE, FORMATEUR, RESPONSABLE_FORMATION

    // Attributs spécifiques pour ELEVE
    private String numTelephoneParent;
    private User.niveauCapacite niveauCapacite;
}