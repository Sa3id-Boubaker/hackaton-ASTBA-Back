package tn.esprit.astba.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.astba.entity.User;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private User.NiveauCapacite niveauCapacite;
}