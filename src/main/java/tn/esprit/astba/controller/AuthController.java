package tn.esprit.astba.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.SignInRequest;
import tn.esprit.astba.dto.SignUpRequest;
import tn.esprit.astba.dto.UserResponse;
import tn.esprit.astba.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    // Inscription (sans token)
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        String message = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    // Connexion (avec token dans cookie)
    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        String token = authService.signIn(request);

        // Créer un cookie avec le token
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);  // Sécurisé : pas accessible via JavaScript
        cookie.setSecure(false);   // Mettez true en production (HTTPS)
        cookie.setPath("/");       // Disponible sur tout le site
        cookie.setMaxAge(24 * 60 * 60); // 24 heures

        response.addCookie(cookie);

        return ResponseEntity.ok("Login successful!");
    }

    // Récupérer l'utilisateur connecté
    @GetMapping("/current-user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    // Déconnexion (supprimer le cookie)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Supprimer le cookie en mettant MaxAge à 0
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expire immédiatement

        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }
}