package tn.esprit.astba.dto;

import lombok.Data;
import tn.esprit.astba.entity.User;

import java.time.LocalDate;

@Data
public class EleveSignUpRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private LocalDate dateNaissance;
    private String numTelephone;
    private String numTelephoneParent;
    private User.niveauCapacite niveauCapacite;
}