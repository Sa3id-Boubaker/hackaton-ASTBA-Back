package tn.esprit.astba.dto;

import lombok.Data;

@Data
public class NiveauCreateRequest {
    private String nom;
    private Integer numeroOrdre; // 1, 2, 3, 4
    private Long formationId;
}