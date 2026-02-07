package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.*;
import tn.esprit.astba.service.PresenceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presences")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PresenceController {

    private final PresenceService presenceService;

    // Marquer une présence (Formateur, Responsable, Admin)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public ResponseEntity<?> marquerPresence(@RequestBody PresenceCreateRequest request) {
        try {
            PresenceResponse presence = presenceService.marquerPresence(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Présence marquée avec succès");
            response.put("status", "success");
            response.put("presence", presence);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Marquer plusieurs présences en une fois (Formateur, Responsable, Admin)
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public ResponseEntity<?> marquerPresencesBulk(@RequestBody PresenceBulkRequest request) {
        try {
            List<PresenceResponse> presences = presenceService.marquerPresencesBulk(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Présences marquées avec succès");
            response.put("status", "success");
            response.put("presences", presences);
            response.put("count", presences.size());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Modifier une présence (Formateur, Responsable, Admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public ResponseEntity<?> updatePresence(@PathVariable Long id, @RequestBody PresenceUpdateRequest request) {
        try {
            PresenceResponse presence = presenceService.updatePresence(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Présence modifiée avec succès");
            response.put("status", "success");
            response.put("presence", presence);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de modification", e.getMessage());
        }
    }

    // Récupérer toutes les présences (Admin, Responsable)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> getAllPresences() {
        try {
            List<PresenceResponse> presences = presenceService.getAllPresences();
            return ResponseEntity.ok(presences);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer une présence par ID (Tous)
    @GetMapping("/{id}")
    public ResponseEntity<?> getPresenceById(@PathVariable Long id) {
        try {
            PresenceResponse presence = presenceService.getPresenceById(id);
            return ResponseEntity.ok(presence);
        } catch (RuntimeException e) {
            return buildErrorResponse("Présence non trouvée", e.getMessage());
        }
    }

    // Récupérer les présences d'un élève (Tous)
    @GetMapping("/eleve/{eleveId}")
    public ResponseEntity<?> getPresencesByEleve(@PathVariable Long eleveId) {
        try {
            List<PresenceResponse> presences = presenceService.getPresencesByEleve(eleveId);
            return ResponseEntity.ok(presences);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer les présences d'une séance (Tous)
    @GetMapping("/seance/{seanceId}")
    public ResponseEntity<?> getPresencesBySeance(@PathVariable Long seanceId) {
        try {
            List<PresenceResponse> presences = presenceService.getPresencesBySeance(seanceId);
            return ResponseEntity.ok(presences);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer les présences d'un élève pour une formation (Tous)
    @GetMapping("/eleve/{eleveId}/formation/{formationId}")
    public ResponseEntity<?> getPresencesByEleveAndFormation(
            @PathVariable Long eleveId,
            @PathVariable Long formationId) {
        try {
            List<PresenceResponse> presences = presenceService.getPresencesByEleveAndFormation(eleveId, formationId);
            return ResponseEntity.ok(presences);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Statistiques d'une séance (Formateur, Responsable, Admin)
    @GetMapping("/seance/{seanceId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public ResponseEntity<?> getSeanceStats(@PathVariable Long seanceId) {
        try {
            PresenceService.PresenceSeanceStatsResponse stats = presenceService.getSeanceStats(seanceId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Supprimer une présence (Admin, Responsable)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> deletePresence(@PathVariable Long id) {
        try {
            String message = presenceService.deletePresence(id);
            return buildSuccessResponse(message);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de suppression", e.getMessage());
        }
    }

    // ============ MÉTHODES UTILITAIRES ============

    private ResponseEntity<?> buildSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> buildErrorResponse(String error, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("status", "error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}