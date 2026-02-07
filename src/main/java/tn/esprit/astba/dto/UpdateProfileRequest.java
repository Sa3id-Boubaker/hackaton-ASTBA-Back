package tn.esprit.astba.dto;

import lombok.Data;
import tn.esprit.astba.entity.User;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String numTelephone;

    // Pour les élèves uniquement
    private String numTelephoneParent;
    private User.niveauCapacite niveauCapacite;
}