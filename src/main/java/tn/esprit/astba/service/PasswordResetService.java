package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.astba.dto.ForgotPasswordRequest;
import tn.esprit.astba.dto.ResetPasswordRequest;
import tn.esprit.astba.entity.User;
import tn.esprit.astba.entity.VerificationCode;
import tn.esprit.astba.repository.UserRepository;
import tn.esprit.astba.repository.VerificationCodeRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // ✅ Injection du service Email

    // Étape 1: Générer et envoyer le code par email
    @Transactional
    public String sendVerificationCode(ForgotPasswordRequest request) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Supprimer l'ancien code s'il existe
        verificationCodeRepository.deleteByEmail(request.getEmail());

        // Générer un code à 4 chiffres
        String code = generateCode();

        // Créer le code de vérification avec hash
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(request.getEmail());
        verificationCode.setCodeHash(passwordEncoder.encode(code));
        verificationCode.setCreatedAt(LocalDateTime.now());
        verificationCode.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        verificationCodeRepository.save(verificationCode);

        // ✅ Envoyer le code par email
        try {
            emailService.sendVerificationCode(request.getEmail(), code);
            return "Verification code sent to your email. Please check your inbox.";
        } catch (Exception e) {
            // Si l'envoi échoue, on retourne quand même le code (pour le développement)
            return "Email sending failed. Code: " + code + " (expires in 15 minutes)";
        }
    }

    // Étape 2: Vérifier le code avec email
    public String verifyCodeWithEmail(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No verification code found for this email"));

        // Vérifier si le code n'a pas expiré
        if (verificationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.deleteByEmail(email);
            throw new RuntimeException("Verification code has expired");
        }

        // Vérifier le code avec BCrypt
        if (!passwordEncoder.matches(code, verificationCode.getCodeHash())) {
            throw new RuntimeException("Invalid verification code");
        }

        return "Code verified successfully";
    }

    // Étape 3: Réinitialiser le mot de passe
    @Transactional
    public String resetPassword(String email, ResetPasswordRequest request) {
        // Vérifier que les mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Vérifier si le code existe toujours
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Please verify your code first"));

        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Supprimer le code de vérification
        verificationCodeRepository.deleteByEmail(email);

        return "Password reset successfully!";
    }

    // Générer un code à 4 chiffres
    private String generateCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }
}