package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.CertificatResponse;
import tn.esprit.astba.service.CertificatService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CertificatController {

    private final CertificatService certificatService;

    // Générer un certificat (Admin, Responsable)
    @PostMapping("/generer/{eleveId}/{formationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> genererCertificat(@PathVariable Long eleveId, @PathVariable Long formationId) {
        try {
            CertificatResponse certificat = certificatService.genererCertificat(eleveId, formationId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Certificat généré avec succès");
            response.put("status", "success");
            response.put("certificat", certificat);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de génération", e.getMessage());
        }
    }

    // Récupérer tous les certificats (Admin, Responsable)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<?> getAllCertificats() {
        try {
            List<CertificatResponse> certificats = certificatService.getAllCertificats();
            return ResponseEntity.ok(certificats);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer un certificat par ID (Tous)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCertificatById(@PathVariable Long id) {
        try {
            CertificatResponse certificat = certificatService.getCertificatById(id);
            return ResponseEntity.ok(certificat);
        } catch (RuntimeException e) {
            return buildErrorResponse("Certificat non trouvé", e.getMessage());
        }
    }

    // Récupérer par numéro (Tous - pour vérification)
    @GetMapping("/numero/{numeroCertificat}")
    public ResponseEntity<?> getCertificatByNumero(@PathVariable String numeroCertificat) {
        try {
            CertificatResponse certificat = certificatService.getCertificatByNumero(numeroCertificat);
            return ResponseEntity.ok(certificat);
        } catch (RuntimeException e) {
            return buildErrorResponse("Certificat non trouvé", e.getMessage());
        }
    }

    // Récupérer les certificats d'un élève (Tous)
    @GetMapping("/eleve/{eleveId}")
    public ResponseEntity<?> getCertificatsByEleve(@PathVariable Long eleveId) {
        try {
            List<CertificatResponse> certificats = certificatService.getCertificatsByEleve(eleveId);
            return ResponseEntity.ok(certificats);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer les certificats d'une formation (Tous)
    @GetMapping("/formation/{formationId}")
    public ResponseEntity<?> getCertificatsByFormation(@PathVariable Long formationId) {
        try {
            List<CertificatResponse> certificats = certificatService.getCertificatsByFormation(formationId);
            return ResponseEntity.ok(certificats);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Vérifier l'éligibilité (Tous)
    @GetMapping("/eligibilite/{eleveId}/{formationId}")
    public ResponseEntity<?> verifierEligibilite(@PathVariable Long eleveId, @PathVariable Long formationId) {
        try {
            CertificatService.EligibiliteCertificatResponse eligibilite =
                    certificatService.verifierEligibilite(eleveId, formationId);
            return ResponseEntity.ok(eligibilite);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Supprimer un certificat (Admin uniquement)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCertificat(@PathVariable Long id) {
        try {
            String message = certificatService.deleteCertificat(id);
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