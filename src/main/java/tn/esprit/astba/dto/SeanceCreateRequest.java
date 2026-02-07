package tn.esprit.astba.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SeanceCreateRequest {
    private String titre;
    private String description;
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer numeroOrdre; // 1 à 6
    private Long niveauId;
}