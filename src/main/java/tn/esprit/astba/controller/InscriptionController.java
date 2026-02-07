package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.InscriptionCreateRequest;
import tn.esprit.astba.dto.InscriptionResponse;
import tn.esprit.astba.entity.Inscription;
import tn.esprit.astba.service.InscriptionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class InscriptionController {

    private final InscriptionService inscriptionService;

    // Créer une inscription (Admin, Responsable)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> createInscription(@RequestBody InscriptionCreateRequest request) {
        try {
            InscriptionResponse inscription = inscriptionService.createInscription(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inscription créée avec succès");
            response.put("status", "success");
            response.put("inscription", inscription);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de création", e.getMessage());
        }
    }

    // Récupérer toutes les inscriptions (Admin, Responsable)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> getAllInscriptions() {
        try {
            List<InscriptionResponse> inscriptions = inscriptionService.getAllInscriptions();
            return ResponseEntity.ok(inscriptions);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer une inscription par ID (Tous)
    @GetMapping("/{id}")
    public ResponseEntity<?> getInscriptionById(@PathVariable Long id) {
        try {
            InscriptionResponse inscription = inscriptionService.getInscriptionById(id);
            return ResponseEntity.ok(inscription);
        } catch (RuntimeException e) {
            return buildErrorResponse("Inscription non trouvée", e.getMessage());
        }
    }

    // Récupérer les inscriptions d'un élève (Tous)
    @GetMapping("/eleve/{eleveId}")
    public ResponseEntity<?> getInscriptionsByEleve(@PathVariable Long eleveId) {
        try {
            List<InscriptionResponse> inscriptions = inscriptionService.getInscriptionsByEleve(eleveId);
            return ResponseEntity.ok(inscriptions);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer les inscriptions d'une formation (Tous)
    @GetMapping("/formation/{formationId}")
    public ResponseEntity<?> getInscriptionsByFormation(@PathVariable Long formationId) {
        try {
            List<InscriptionResponse> inscriptions = inscriptionService.getInscriptionsByFormation(formationId);
            return ResponseEntity.ok(inscriptions);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer par statut (Admin, Responsable)
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> getInscriptionsByStatut(@PathVariable String statut) {
        try {
            Inscription.Statut statutEnum = Inscription.Statut.valueOf(statut.toUpperCase());
            List<InscriptionResponse> inscriptions = inscriptionService.getInscriptionsByStatut(statutEnum);
            return ResponseEntity.ok(inscriptions);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Statut invalide", "Le statut spécifié n'existe pas");
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Terminer une inscription (Admin, Responsable)
    @PutMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> terminerInscription(@PathVariable Long id) {
        try {
            InscriptionResponse inscription = inscriptionService.terminerInscription(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inscription terminée avec succès");
            response.put("status", "success");
            response.put("inscription", inscription);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Supprimer une inscription (Admin, Responsable)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> deleteInscription(@PathVariable Long id) {
        try {
            String message = inscriptionService.deleteInscription(id);
            return buildSuccessResponse(message);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de suppression", e.getMessage());
        }
    }

    // Statistiques (Admin, Responsable)
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> getStatistics() {
        try {
            InscriptionService.InscriptionStatsResponse stats = inscriptionService.getStatistics();
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