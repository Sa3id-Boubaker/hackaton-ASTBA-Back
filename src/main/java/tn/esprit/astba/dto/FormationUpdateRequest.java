package tn.esprit.astba.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FormationUpdateRequest {
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long formateurId;
}