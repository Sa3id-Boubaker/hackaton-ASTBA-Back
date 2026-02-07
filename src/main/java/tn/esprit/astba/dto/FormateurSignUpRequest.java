package tn.esprit.astba.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FormateurSignUpRequest {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private LocalDate dateNaissance;
    private String numTelephone;
}