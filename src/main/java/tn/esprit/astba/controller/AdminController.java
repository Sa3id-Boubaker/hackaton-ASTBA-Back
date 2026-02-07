package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.AdminCreateUserRequest;
import tn.esprit.astba.dto.AdminUpdateUserRequest;
import tn.esprit.astba.dto.UserResponse;
import tn.esprit.astba.entity.User;
import tn.esprit.astba.service.AdminService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ============ GESTION DES UTILISATEURS ============

    // Récupérer tous les utilisateurs
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponse> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer un utilisateur par ID
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserResponse user = adminService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return buildErrorResponse("Utilisateur non trouvé", e.getMessage());
        }
    }

    // Récupérer les utilisateurs en attente (status = false)
    @GetMapping("/users/pending")
    public ResponseEntity<?> getPendingUsers() {
        try {
            List<UserResponse> users = adminService.getUsersByStatus(false);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer les utilisateurs actifs (status = true)
    @GetMapping("/users/active")
    public ResponseEntity<?> getActiveUsers() {
        try {
            List<UserResponse> users = adminService.getUsersByStatus(true);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // Récupérer les utilisateurs par rôle
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<UserResponse> users = adminService.getUsersByRole(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Rôle invalide", "Le rôle spécifié n'existe pas");
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur", e.getMessage());
        }
    }

    // ============ CRÉATION ET MODIFICATION ============

    // Créer un nouvel utilisateur (Élève, Formateur, Responsable)
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AdminCreateUserRequest request) {
        try {
            UserResponse user = adminService.createUser(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur créé avec succès");
            response.put("status", "success");
            response.put("user", user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de création", e.getMessage());
        }
    }

    // Modifier un utilisateur existant
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody AdminUpdateUserRequest request) {
        try {
            UserResponse user = adminService.updateUser(userId, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur modifié avec succès");
            response.put("status", "success");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de modification", e.getMessage());
        }
    }

    // ============ ACTIONS SUR LES UTILISATEURS ============

    // Activer un utilisateur (false → true)
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            String message = adminService.toggleUserStatus(userId, true);
            return buildSuccessResponse(message);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur d'activation", e.getMessage());
        }
    }

    // Désactiver un utilisateur (true → false)
    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            String message = adminService.toggleUserStatus(userId, false);
            return buildSuccessResponse(message);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de désactivation", e.getMessage());
        }
    }

    // Supprimer un utilisateur
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            String message = adminService.deleteUser(userId);
            return buildSuccessResponse(message);
        } catch (RuntimeException e) {
            return buildErrorResponse("Erreur de suppression", e.getMessage());
        }
    }

    // ============ STATISTIQUES ============

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            AdminService.AdminStatsResponse stats = adminService.getStatistics();
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