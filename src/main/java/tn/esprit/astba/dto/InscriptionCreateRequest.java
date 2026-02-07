package tn.esprit.astba.dto;

import lombok.Data;

@Data
public class InscriptionCreateRequest {
    private Long eleveId;
    private Long formationId;
}