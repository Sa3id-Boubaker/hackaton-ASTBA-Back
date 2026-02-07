package tn.esprit.astba.dto;

import lombok.Data;
import tn.esprit.astba.entity.User;

import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private LocalDate dateNaissance;
    private String numTelephone;
    private User.Role role;
    private Boolean status;

    // Pour les élèves
    private String numTelephoneParent;
    private User.niveauCapacite niveauCapacite;
}