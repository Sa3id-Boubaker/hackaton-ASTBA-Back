package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.astba.dto.AuthResponse;
import tn.esprit.astba.dto.SignInRequest;
import tn.esprit.astba.dto.SignUpRequest;
import tn.esprit.astba.dto.UserResponse;
import tn.esprit.astba.entity.User;
import tn.esprit.astba.repository.UserRepository;
import tn.esprit.astba.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Inscription (SANS génération de token)
    public String signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateNaissance(request.getDateNaissance());
        user.setNumTelephone(request.getNumTelephone());
        user.setRole(request.getRole());
        user.setStatus(true);

        // Si c'est un élève
        if (request.getRole() == User.Role.ELEVE) {
            user.setNumTelephoneParent(request.getNumTelephoneParent());
            user.setNiveauCapacite(request.getNiveauCapacite());
        }

        userRepository.save(user);

        return "User registered successfully! Please login.";
    }

    // Connexion (AVEC génération de token)
    public String signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getStatus()) {
            throw new RuntimeException("Account is disabled!");
        }

        // Générer le token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return token;
    }

    // Récupérer l'utilisateur connecté
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
}