package tn.esprit.astba.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.*;
import tn.esprit.astba.service.AuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    // Inscription pour FORMATEUR
    @PostMapping("/signup/formateur")
    public ResponseEntity<?> signUpFormateur(@RequestBody FormateurSignUpRequest request) {
        try {
            SignUpResponse response = authService.signUpFormateur(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur d'inscription");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    // Inscription pour RESPONSABLE_FORMATION
    @PostMapping("/signup/responsable")
    public ResponseEntity<?> signUpResponsable(@RequestBody ResponsableSignUpRequest request) {
        try {
            SignUpResponse response = authService.signUpResponsable(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur d'inscription");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    // Inscription pour ELEVE
    @PostMapping("/signup/eleve")
    public ResponseEntity<?> signUpEleve(@RequestBody EleveSignUpRequest request) {
        try {
            SignUpResponse response = authService.signUpEleve(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur d'inscription");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    // Connexion (COMMUN à tous les rôles)
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        try {
            SignInResponse signInResponse = authService.signIn(request);

            // Créer un cookie avec le token
            Cookie cookie = new Cookie("jwt", signInResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);

            response.addCookie(cookie);

            return ResponseEntity.ok(signInResponse); // ✅ Retourner l'objet complet

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur de connexion");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // Récupérer l'utilisateur connecté
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            UserResponse response = authService.getCurrentUser();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Utilisateur non authentifié");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // Déconnexion (COMMUN à tous les rôles)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Supprimer le cookie en mettant MaxAge à 0
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expire immédiatement

        response.addCookie(cookie);

        return ResponseEntity.ok("Déconnexion réussie !");
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            UserResponse response = authService.updateProfile(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur de mise à jour");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request) {
        try {
            String message = authService.updatePassword(request);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", message);
            successResponse.put("status", "success");

            return ResponseEntity.ok(successResponse);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur de mise à jour du mot de passe");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

}