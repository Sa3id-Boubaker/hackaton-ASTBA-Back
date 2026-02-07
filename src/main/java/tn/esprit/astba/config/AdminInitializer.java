package tn.esprit.astba.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.astba.entity.User;
import tn.esprit.astba.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Créer l'admin si n'existe pas
        if (!userRepository.existsByEmail("admin@astba.tn")) {
            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("ASTBA");
            admin.setEmail("admin@astba.tn");
            admin.setPassword(passwordEncoder.encode("Admin@2024"));
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(true);

            userRepository.save(admin);
            System.out.println("✅ Admin créé: admin@astba.tn / Admin@2024");
        }
    }
}