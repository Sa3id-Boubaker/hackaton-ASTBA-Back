package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.astba.dto.*;
import tn.esprit.astba.entity.User;
import tn.esprit.astba.repository.UserRepository;
import tn.esprit.astba.security.JwtUtil;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ============ MÉTHODE COMMUNE DE VALIDATION ============
    private void validatePasswordStrength(String password) {
        // Règles de sécurité
        if (password.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }

        // Vérifier la présence d'au moins une majuscule
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins une lettre majuscule");
        }

        // Vérifier la présence d'au moins une minuscule
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins une lettre minuscule");
        }

        // Vérifier la présence d'au moins un chiffre
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins un chiffre");
        }

        // Vérifier la présence d'au moins un caractère spécial
        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&* etc.)");
        }

        // Vérifier qu'il n'y a pas d'espaces
        if (password.contains(" ")) {
            throw new RuntimeException("Le mot de passe ne doit pas contenir d'espaces");
        }

        // Vérifier les mots de passe courants/faibles
        if (isWeakPassword(password)) {
            throw new RuntimeException("Le mot de passe est trop commun, veuillez en choisir un plus sécurisé");
        }
    }

    // Vérifier les mots de passe faibles/courants
    private boolean isWeakPassword(String password) {
        String[] weakPasswords = {
                "password", "password123", "12345678", "azerty", "qwerty",
                "admin123", "welcome123", "letmein", "sunshine", "iloveyou"
        };

        String lowerCasePassword = password.toLowerCase();
        for (String weak : weakPasswords) {
            if (lowerCasePassword.contains(weak)) {
                return true;
            }
        }
        return false;
    }

    // ============ INSCRIPTION FORMATEUR ============
    public SignUpResponse signUpFormateur(FormateurSignUpRequest request) {
        // VALIDER LE MOT DE PASSE
        validatePasswordStrength(request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String message = String.format(
                    "Un compte avec l'email '%s' existe déjà. Ce compte est un %s.",
                    request.getEmail(),
                    getFrenchRoleName(existingUser.getRole())
            );
            throw new RuntimeException(message);
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateNaissance(request.getDateNaissance());
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(User.Role.FORMATEUR);
        user.setStatus(false);

        userRepository.save(user);

        return new SignUpResponse(
                "Formateur inscrit avec succès !",
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ============ INSCRIPTION RESPONSABLE ============
    public SignUpResponse signUpResponsable(ResponsableSignUpRequest request) {
        // VALIDER LE MOT DE PASSE
        validatePasswordStrength(request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String message = String.format(
                    "Un compte avec l'email '%s' existe déjà. Ce compte est un %s.",
                    request.getEmail(),
                    getFrenchRoleName(existingUser.getRole())
            );
            throw new RuntimeException(message);
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateNaissance(request.getDateNaissance());
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(User.Role.RESPONSABLE_FORMATION);
        user.setStatus(false);

        userRepository.save(user);

        return new SignUpResponse(
                "Responsable de formation inscrit avec succès !",
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ============ INSCRIPTION ÉLÈVE ============
    public SignUpResponse signUpEleve(EleveSignUpRequest request) {
        // VALIDER LE MOT DE PASSE
        validatePasswordStrength(request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String message = String.format(
                    "Un compte avec l'email '%s' existe déjà. Ce compte est un %s.",
                    request.getEmail(),
                    getFrenchRoleName(existingUser.getRole())
            );
            throw new RuntimeException(message);
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateNaissance(request.getDateNaissance());
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(User.Role.ELEVE);
        user.setStatus(false);

        // Attributs spécifiques à l'élève
        user.setNumTelephoneParent(request.getNumTelephoneParent());
        user.setNiveauCapacite(request.getNiveauCapacite());

        userRepository.save(user);

        return new SignUpResponse(
                "Élève inscrit avec succès !",
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ============ CONNEXION ============
    public SignInResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getStatus()) {
            throw new RuntimeException("Account is disabled!");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        UserResponse userResponse = convertToUserResponse(user);

        // ✅ Vérifier si l'utilisateur doit changer son mot de passe
        SignInResponse response = new SignInResponse();
        response.setToken(token);
        response.setUser(userResponse);
        response.setMustChangePassword(user.getMustChangePassword()); // ✅ NOUVEAU

        if (user.getMustChangePassword()) {
            response.setMessage("Connexion réussie ! Vous devez changer votre mot de passe.");
        } else {
            response.setMessage("Connexion réussie !");
        }

        return response;
    }

    // ============ RÉCUPÉRER UTILISATEUR ============
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setEmail(user.getEmail());
        response.setDateNaissance(user.getDateNaissance());
        response.setNumTelephone(user.getNumTelephone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());

        if (user.getRole() == User.Role.ELEVE) {
            response.setNumTelephoneParent(user.getNumTelephoneParent());
            response.setNiveauCapacite(user.getNiveauCapacite());
        }

        return response;
    }

    // ============ MODIFIER PROFIL ============
    public UserResponse updateProfile(UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setDateNaissance(request.getDateNaissance());
        user.setNumTelephone(request.getNumTelephone());

        if (user.getRole() == User.Role.ELEVE) {
            user.setNumTelephoneParent(request.getNumTelephoneParent());
            user.setNiveauCapacite(request.getNiveauCapacite());
        }

        userRepository.save(user);

        return convertToUserResponse(user);
    }

    // ============ MODIFIER MOT DE PASSE ============
    public String updatePassword(UpdatePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 1. Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        // 2. Vérifier que les nouveaux mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les nouveaux mots de passe ne correspondent pas");
        }

        // 3. Vérifier que le nouveau mot de passe est différent de l'ancien
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // 4. Valider la force du mot de passe
        validatePasswordStrength(request.getNewPassword());

        // 5. Mettre à jour
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // ✅ Retirer l'obligation de changer le mot de passe
        userRepository.save(user);

        return "Mot de passe mis à jour avec succès";
    }

    // ============ MÉTHODES UTILITAIRES ============
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setEmail(user.getEmail());
        response.setDateNaissance(user.getDateNaissance());
        response.setNumTelephone(user.getNumTelephone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());

        if (user.getRole() == User.Role.ELEVE) {
            response.setNumTelephoneParent(user.getNumTelephoneParent());
            response.setNiveauCapacite(user.getNiveauCapacite());
        }

        return response;
    }

    private String getFrenchRoleName(User.Role role) {
        switch (role) {
            case ADMIN:
                return "Administrateur";
            case FORMATEUR:
                return "Formateur";
            case RESPONSABLE_FORMATION:
                return "Responsable de formation";
            case ELEVE:
                return "Élève";
            default:
                return role.name();
        }
    }

}