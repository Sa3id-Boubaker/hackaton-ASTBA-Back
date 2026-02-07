package tn.esprit.astba.dto;

import lombok.Data;

@Data
public class NiveauUpdateRequest {
    private String nom;
    private Integer numeroOrdre;
}