package tn.esprit.astba.dto;

import lombok.Data;
import tn.esprit.astba.entity.User;

import java.time.LocalDate;

@Data
public class SignUpRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private LocalDate dateNaissance;
    private String numTelephone;
    private User.Role role;

    // Pour les élèves
    private String numTelephoneParent;
    private User.niveauCapacite niveauCapacite;
}