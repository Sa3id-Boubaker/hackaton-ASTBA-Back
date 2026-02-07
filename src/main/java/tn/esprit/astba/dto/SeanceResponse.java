package tn.esprit.astba.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SeanceResponse {
    private Long id;
    private String titre;
    private String description;
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer numeroOrdre;
    private Long niveauId;
    private String niveauNom;
    private Integer niveauNumeroOrdre;
    private Long formationId;
    private String formationNom;
    private int nombrePresences;
}