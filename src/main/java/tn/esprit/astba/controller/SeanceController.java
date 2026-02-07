package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.SeanceCreateRequest;
import tn.esprit.astba.dto.SeanceResponse;
import tn.esprit.astba.dto.SeanceUpdateRequest;
import tn.esprit.astba.service.SeanceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SeanceController {

    private final SeanceService seanceService;

    // Créer une séance (Admin, Responsable, Formateur)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public ResponseEntity<?> createSeance(@RequestBody SeanceCreateRequest request) {
        try {
            SeanceResponse seance = seanceService.createSeance(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Séance créée avec succès");
            response.put("status", "success");
            response.put("seance", seance);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de création", e.getMessage());
        }
    }

    // Modifier une séance (Admin, Responsable, Formateur)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public ResponseEntity<?> updateSeance(@PathVariable Long id, @RequestBody SeanceUpdateRequest request) {
        try {
            SeanceResponse seance = seanceService.updateSeance(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Séance modifiée avec succès");
            response.put("status", "success");
            response.put("seance", seance);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de modification", e.getMessage());
        }
    }

    // Récupérer toutes les séances (Tous)
    @GetMapping
    public ResponseEntity<?> getAllSeances() {
        try {
            List<SeanceResponse> seances = seanceService.getAllSeances();
            return ResponseEntity.ok(seances);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer une séance par ID (Tous)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSeanceById(@PathVariable Long id) {
        try {
            SeanceResponse seance = seanceService.getSeanceById(id);
            return ResponseEntity.ok(seance);
        } catch (RuntimeException e) {
            return buildErrorResponse("Séance non trouvée", e.getMessage());
        }
    }

    // Récupérer les séances d'un niveau (Tous)
    @GetMapping("/niveau/{niveauId}")
    public ResponseEntity<?> getSeancesByNiveau(@PathVariable Long niveauId) {
        try {
            List<SeanceResponse> seances = seanceService.getSeancesByNiveau(niveauId);
            return ResponseEntity.ok(seances);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Supprimer une séance (Admin, Responsable)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> deleteSeance(@PathVariable Long id) {
        try {
            String message = seanceService.deleteSeance(id);
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