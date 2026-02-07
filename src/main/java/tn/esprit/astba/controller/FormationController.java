package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.FormationCreateRequest;
import tn.esprit.astba.dto.FormationResponse;
import tn.esprit.astba.dto.FormationUpdateRequest;
import tn.esprit.astba.service.FormationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class FormationController {

    private final FormationService formationService;

    // ============ CRUD DE BASE ============

    // Créer une formation (Admin ou Responsable)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> createFormation(@RequestBody FormationCreateRequest request) {
        try {
            FormationResponse formation = formationService.createFormation(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Formation créée avec succès");
            response.put("status", "success");
            response.put("formation", formation);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de création", e.getMessage());
        }
    }

    // Modifier une formation (Admin ou Responsable)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> updateFormation(@PathVariable Long id, @RequestBody FormationUpdateRequest request) {
        try {
            FormationResponse formation = formationService.updateFormation(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Formation modifiée avec succès");
            response.put("status", "success");
            response.put("formation", formation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de modification", e.getMessage());
        }
    }

    // Récupérer toutes les formations (Tous les utilisateurs authentifiés)
    @GetMapping
    public ResponseEntity<?> getAllFormations() {
        try {
            List<FormationResponse> formations = formationService.getAllFormations();
            return ResponseEntity.ok(formations);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer une formation par ID (Tous les utilisateurs authentifiés)
    @GetMapping("/{id}")
    public ResponseEntity<?> getFormationById(@PathVariable Long id) {
        try {
            FormationResponse formation = formationService.getFormationById(id);
            return ResponseEntity.ok(formation);
        } catch (RuntimeException e) {
            return buildErrorResponse("Formation non trouvée", e.getMessage());
        }
    }

    // Rechercher par nom (Tous les utilisateurs authentifiés)
    @GetMapping("/search")
    public ResponseEntity<?> searchFormations(@RequestParam String nom) {
        try {
            List<FormationResponse> formations = formationService.searchFormationsByNom(nom);
            return ResponseEntity.ok(formations);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de recherche", e.getMessage());
        }
    }

    // Supprimer une formation (Admin ou Responsable)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> deleteFormation(@PathVariable Long id) {
        try {
            String message = formationService.deleteFormation(id);
            return buildSuccessResponse(message);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de suppression", e.getMessage());
        }
    }

    // ============ GESTION DES FORMATEURS ============

    // Récupérer les formations d'un formateur
    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<?> getFormationsByFormateur(@PathVariable Long formateurId) {
        try {
            List<FormationResponse> formations = formationService.getFormationsByFormateur(formateurId);
            return ResponseEntity.ok(formations);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Assigner un formateur à une formation (Admin ou Responsable)
    @PutMapping("/{formationId}/formateur/{formateurId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> assignFormateur(@PathVariable Long formationId, @PathVariable Long formateurId) {
        try {
            FormationResponse formation = formationService.assignFormateur(formationId, formateurId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Formateur assigné avec succès");
            response.put("status", "success");
            response.put("formation", formation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur d'assignation", e.getMessage());
        }
    }

    // Retirer le formateur d'une formation (Admin ou Responsable)
    @DeleteMapping("/{formationId}/formateur")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> removeFormateur(@PathVariable Long formationId) {
        try {
            FormationResponse formation = formationService.removeFormateur(formationId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Formateur retiré avec succès");
            response.put("status", "success");
            response.put("formation", formation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // ============ STATISTIQUES ============

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> getStatistics() {
        try {
            FormationService.FormationStatsResponse stats = formationService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
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