package tn.esprit.astba.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PresenceResponse {
    private Long id;

    // Informations de l'élève
    private Long eleveId;
    private String eleveNom;
    private String elevePrenom;
    private String eleveEmail;

    // Informations de la séance
    private Long seanceId;
    private String seanceTitre;
    private Integer seanceNumeroOrdre;

    // Informations du niveau
    private Long niveauId;
    private String niveauNom;
    private Integer niveauNumeroOrdre;

    // Informations de la formation
    private Long formationId;
    private String formationNom;

    private Boolean present;
    private LocalDateTime dateMarquage;
}