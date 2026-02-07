package tn.esprit.astba.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.astba.entity.User;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private User.NiveauCapacite niveauCapacite;
}