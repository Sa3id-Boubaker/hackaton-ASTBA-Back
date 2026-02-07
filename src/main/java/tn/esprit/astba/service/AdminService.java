package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.astba.dto.AdminCreateUserRequest;
import tn.esprit.astba.dto.AdminUpdateUserRequest;
import tn.esprit.astba.dto.UserResponse;
import tn.esprit.astba.entity.User;
import tn.esprit.astba.repository.UserRepository;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService; // ✅ NOUVEAU
    private final EmailService emailService;

    // ============ VALIDATION DU MOT DE PASSE ============
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }

        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins une lettre majuscule");
        }

        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins une lettre minuscule");
        }

        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins un chiffre");
        }

        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            throw new RuntimeException("Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&* etc.)");
        }

        if (password.contains(" ")) {
            throw new RuntimeException("Le mot de passe ne doit pas contenir d'espaces");
        }
    }

    // ============ CRÉER UN UTILISATEUR (ADMIN) ============
    public UserResponse createUser(AdminCreateUserRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Validation du rôle
        if (request.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("Impossible de créer un administrateur via cette méthode");
        }

        // ✅ Générer un mot de passe fort automatiquement
        String tempPassword = passwordGeneratorService.generateStrongPassword();

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(tempPassword)); // Hasher le mot de passe généré
        user.setDateNaissance(request.getDateNaissance());
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(request.getRole());
        user.setStatus(true);
        user.setMustChangePassword(true); // ✅ Forcer le changement de mot de passe

        // Si c'est un élève, ajouter les attributs spécifiques
        if (request.getRole() == User.Role.ELEVE) {
            user.setNumTelephoneParent(request.getNumTelephoneParent());
            user.setNiveauCapacite(request.getNiveauCapacite());
        }

        User savedUser = userRepository.save(user);

        // ✅ Envoyer le mot de passe par email
        try {
            emailService.sendTemporaryPassword(
                    savedUser.getEmail(),
                    savedUser.getNom(),
                    savedUser.getPrenom(),
                    tempPassword
            );
        } catch (Exception e) {
            // En cas d'erreur d'envoi d'email, on log mais on ne bloque pas la création
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }

        return convertToUserResponse(savedUser);
    }

    // ============ MODIFIER UN UTILISATEUR (ADMIN) ============
    public UserResponse updateUser(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier qu'on ne modifie pas un admin
        if (user.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("Impossible de modifier un administrateur via cette méthode");
        }

        // Vérifier si l'email est changé et s'il existe déjà
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà");
            }
            user.setEmail(request.getEmail());
        }

        // Validation du nouveau rôle
        if (request.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("Impossible de promouvoir un utilisateur en administrateur");
        }

        // Mettre à jour les informations
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setDateNaissance(request.getDateNaissance());
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(request.getRole());

        // Mettre à jour le status seulement s'il est fourni
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        // Si le nouveau rôle est ELEVE, mettre à jour les attributs spécifiques
        if (request.getRole() == User.Role.ELEVE) {
            user.setNumTelephoneParent(request.getNumTelephoneParent());
            user.setNiveauCapacite(request.getNiveauCapacite());
        } else {
            // Si ce n'est plus un élève, nettoyer les attributs
            user.setNumTelephoneParent(null);
            user.setNiveauCapacite(null);
        }

        User updatedUser = userRepository.save(user);

        return convertToUserResponse(updatedUser);
    }

    // ============ RÉCUPÉRER LES UTILISATEURS PAR STATUS ============
    public List<UserResponse> getUsersByStatus(boolean status) {
        return userRepository.findByStatus(status).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER TOUS LES UTILISATEURS ============
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER LES UTILISATEURS PAR RÔLE ============
    public List<UserResponse> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER UN UTILISATEUR PAR ID ============
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return convertToUserResponse(user);
    }

    // ============ ACTIVER/DÉSACTIVER UTILISATEUR ============
    public String toggleUserStatus(Long userId, boolean activate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (activate && user.getStatus()) {
            throw new RuntimeException("Cet utilisateur est déjà actif");
        }

        if (!activate && !user.getStatus()) {
            throw new RuntimeException("Cet utilisateur est déjà désactivé");
        }

        user.setStatus(activate);
        userRepository.save(user);

        String action = activate ? "activé" : "désactivé";
        return String.format("Utilisateur %s %s (%s) a été %s avec succès",
                user.getPrenom(), user.getNom(), user.getEmail(), action);
    }

    // ============ SUPPRIMER UN UTILISATEUR ============
    public String deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier qu'on ne supprime pas un admin
        if (user.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("Impossible de supprimer un administrateur");
        }

        String userName = user.getPrenom() + " " + user.getNom();
        userRepository.delete(user);

        return String.format("Utilisateur %s a été supprimé avec succès", userName);
    }

    // ============ STATISTIQUES ============
    public AdminStatsResponse getStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(true);
        long pendingUsers = userRepository.countByStatus(false);
        long totalEleves = userRepository.countByRole(User.Role.ELEVE);
        long totalFormateurs = userRepository.countByRole(User.Role.FORMATEUR);
        long totalResponsables = userRepository.countByRole(User.Role.RESPONSABLE_FORMATION);

        return new AdminStatsResponse(
                totalUsers,
                activeUsers,
                pendingUsers,
                totalEleves,
                totalFormateurs,
                totalResponsables
        );
    }

    // ============ MÉTHODE UTILITAIRE ============
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

    // ============ DTO POUR LES STATISTIQUES ============
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class AdminStatsResponse {
        private long totalUsers;
        private long activeUsers;
        private long pendingUsers;
        private long totalEleves;
        private long totalFormateurs;
        private long totalResponsables;
    }
}