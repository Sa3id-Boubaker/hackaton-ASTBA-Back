package tn.esprit.astba.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.astba.dto.ForgotPasswordRequest;
import tn.esprit.astba.dto.MessageResponse;
import tn.esprit.astba.dto.ResetPasswordRequest;
import tn.esprit.astba.service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // Variable temporaire pour stocker l'email (en production, utiliser une session ou JWT)
    private String tempEmail;

    // Étape 1: Envoyer le code de vérification
    @PostMapping("/forgot")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String message = passwordResetService.sendVerificationCode(request);
        tempEmail = request.getEmail(); // Sauvegarder l'email temporairement
        return ResponseEntity.ok(new MessageResponse(message, "success"));
    }

    // Étape 2: Vérifier le code
    @PostMapping("/verify-code")
    public ResponseEntity<MessageResponse> verifyCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        if (tempEmail == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Please request a verification code first", "error"));
        }

        String message = passwordResetService.verifyCodeWithEmail(tempEmail, code);
        return ResponseEntity.ok(new MessageResponse(message, "success"));
    }

    // Étape 3: Réinitialiser le mot de passe
    @PostMapping("/reset")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (tempEmail == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Please verify your code first", "error"));
        }

        String message = passwordResetService.resetPassword(tempEmail, request);
        tempEmail = null; // Nettoyer l'email temporaire
        return ResponseEntity.ok(new MessageResponse(message, "success"));
    }
}