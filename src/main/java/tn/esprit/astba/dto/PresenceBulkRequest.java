package tn.esprit.astba.dto;

import lombok.Data;
import java.util.List;

@Data
public class PresenceBulkRequest {
    private Long seanceId;
    private List<ElevePresence> presences;

    @Data
    public static class ElevePresence {
        private Long eleveId;
        private Boolean present;
    }
}