package tn.esprit.astba.dto;

import lombok.Data;
import tn.esprit.astba.entity.Inscription;
import java.time.LocalDate;

@Data
public class InscriptionResponse {
    private Long id;

    // Informations de l'élève
    private Long eleveId;
    private String eleveNom;
    private String elevePrenom;
    private String eleveEmail;

    // Informations de la formation
    private Long formationId;
    private String formationNom;
    private String formationDescription;

    private LocalDate dateInscription;
    private Inscription.Statut statut;

    // Progression
    private int nombreNiveauxTotal;
    private int nombreSeancesTotal;
    private int nombrePresences;
    private double tauxPresence; // Pourcentage
}