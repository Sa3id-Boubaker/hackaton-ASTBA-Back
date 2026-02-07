package tn.esprit.astba.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FormationResponse {
    private Long id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // Informations du formateur
    private Long formateurId;
    private String formateurNom;
    private String formateurPrenom;
    private String formateurEmail;

    // Statistiques
    private int nombreNiveaux;
    private int nombreInscrits;
}