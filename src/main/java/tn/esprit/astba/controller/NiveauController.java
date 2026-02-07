package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.NiveauCreateRequest;
import tn.esprit.astba.dto.NiveauResponse;
import tn.esprit.astba.dto.NiveauUpdateRequest;
import tn.esprit.astba.service.NiveauService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/niveaux")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class NiveauController {

    private final NiveauService niveauService;

    // Créer un niveau (Admin ou Responsable)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> createNiveau(@RequestBody NiveauCreateRequest request) {
        try {
            NiveauResponse niveau = niveauService.createNiveau(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Niveau créé avec succès");
            response.put("status", "success");
            response.put("niveau", niveau);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de création", e.getMessage());
        }
    }

    // Modifier un niveau (Admin ou Responsable)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> updateNiveau(@PathVariable Long id, @RequestBody NiveauUpdateRequest request) {
        try {
            NiveauResponse niveau = niveauService.updateNiveau(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Niveau modifié avec succès");
            response.put("status", "success");
            response.put("niveau", niveau);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de modification", e.getMessage());
        }
    }

    // Récupérer tous les niveaux (Tous)
    @GetMapping
    public ResponseEntity<?> getAllNiveaux() {
        try {
            List<NiveauResponse> niveaux = niveauService.getAllNiveaux();
            return ResponseEntity.ok(niveaux);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer un niveau par ID (Tous)
    @GetMapping("/{id}")
    public ResponseEntity<?> getNiveauById(@PathVariable Long id) {
        try {
            NiveauResponse niveau = niveauService.getNiveauById(id);
            return ResponseEntity.ok(niveau);
        } catch (RuntimeException e) {
            return buildErrorResponse("Niveau non trouvé", e.getMessage());
        }
    }

    // Récupérer les niveaux d'une formation (Tous)
    @GetMapping("/formation/{formationId}")
    public ResponseEntity<?> getNiveauxByFormation(@PathVariable Long formationId) {
        try {
            List<NiveauResponse> niveaux = niveauService.getNiveauxByFormation(formationId);
            return ResponseEntity.ok(niveaux);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Supprimer un niveau (Admin ou Responsable)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> deleteNiveau(@PathVariable Long id) {
        try {
            String message = niveauService.deleteNiveau(id);
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