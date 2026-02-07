package tn.esprit.astba.dto;

import lombok.Data;

@Data
public class PresenceCreateRequest {
    private Long eleveId;
    private Long seanceId;
    private Boolean present;
}