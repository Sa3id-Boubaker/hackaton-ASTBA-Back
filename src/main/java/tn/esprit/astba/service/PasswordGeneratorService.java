package tn.esprit.astba.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;

    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un mot de passe fort aléatoire
     * Format: Au moins 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial
     * Longueur: 12 caractères
     */
    public String generateStrongPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        // Garantir au moins un caractère de chaque type
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Remplir le reste aléatoirement
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Mélanger les caractères pour plus de sécurité
        return shuffleString(password.toString());
    }

    /**
     * Mélange les caractères d'une chaîne
     */
    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}