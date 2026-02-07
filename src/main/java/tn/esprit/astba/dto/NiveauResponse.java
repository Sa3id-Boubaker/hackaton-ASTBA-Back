package tn.esprit.astba.dto;

import lombok.Data;

@Data
public class NiveauResponse {
    private Long id;
    private String nom;
    private Integer numeroOrdre;
    private Long formationId;
    private String formationNom;
    private int nombreSeances;
}