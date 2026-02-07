package tn.esprit.astba.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CertificatResponse {
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

    private LocalDate dateEmission;
    private String numeroCertificat;
    private Boolean genere;
}