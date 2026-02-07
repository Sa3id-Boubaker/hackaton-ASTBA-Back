package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Envoyer un email simple (texte)
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mohamedsaidboubaker10@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    // Envoyer un email HTML
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("mohamedsaidboubaker10@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    // Envoyer le code de vérification
    public void sendVerificationCode(String to, String code) {
        String subject = "Code de réinitialisation de mot de passe - ASTBA";

        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 50px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #333; }
                    .code-box { background-color: #007bff; color: white; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; border-radius: 5px; margin: 30px 0; letter-spacing: 5px; }
                    .content { color: #555; line-height: 1.6; }
                    .footer { margin-top: 30px; text-align: center; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>🔐 Réinitialisation de mot de passe</h1></div>
                    <div class="content">
                        <p>Bonjour,</p>
                        <p>Vous avez demandé la réinitialisation de votre mot de passe pour votre compte ASTBA.</p>
                        <p>Voici votre code de vérification :</p>
                    </div>
                    <div class="code-box">""" + code + """
                    </div>
                    <div class="content">
                        <p><strong>⏰ Ce code expire dans 15 minutes.</strong></p>
                        <p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 ASTBA - Association Sciences and Technology Ben Arous</p>
                    </div>
                </div>
            </body>
            </html>
            """;

        try {
            sendHtmlEmail(to, subject, htmlBody);
        } catch (MessagingException e) {
            String simpleBody = "Votre code de vérification ASTBA est : " + code + "\n\n" +
                    "Ce code expire dans 15 minutes.";
            sendSimpleEmail(to, subject, simpleBody);
        }
    }

    // ✅ NOUVEAU : Envoyer le mot de passe temporaire
    public void sendTemporaryPassword(String to, String nom, String prenom, String tempPassword) {
        String subject = "Bienvenue à ASTBA - Votre compte a été créé";

        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 50px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #333; }
                    .password-box { background-color: #28a745; color: white; font-size: 24px; font-weight: bold; text-align: center; padding: 20px; border-radius: 5px; margin: 30px 0; letter-spacing: 3px; font-family: monospace; }
                    .content { color: #555; line-height: 1.6; }
                    .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; color: #856404; }
                    .footer { margin-top: 30px; text-align: center; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 Bienvenue à ASTBA !</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>""" + prenom + " " + nom + """
                        </strong>,</p>
                        <p>Votre compte ASTBA a été créé avec succès par l'administrateur.</p>
                        <p>Voici vos identifiants de connexion :</p>
                        <p><strong>Email :</strong> """ + to + """
                        </p>
                        <p><strong>Mot de passe temporaire :</strong></p>
                    </div>
                    <div class="password-box">""" + tempPassword + """
                    </div>
                    <div class="warning">
                        <p><strong>⚠️ IMPORTANT :</strong></p>
                        <ul>
                            <li>Ce mot de passe est <strong>temporaire</strong></li>
                            <li>Vous devrez le <strong>changer</strong> lors de votre première connexion</li>
                            <li>Ne partagez jamais votre mot de passe</li>
                        </ul>
                    </div>
                    <div class="content">
                        <p>Pour vous connecter, rendez-vous sur la plateforme ASTBA et utilisez ces identifiants.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 ASTBA - Association Sciences and Technology Ben Arous</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """;

        try {
            sendHtmlEmail(to, subject, htmlBody);
        } catch (MessagingException e) {
            String simpleBody = String.format(
                    "Bonjour %s %s,\n\n" +
                            "Votre compte ASTBA a été créé.\n\n" +
                            "Email : %s\n" +
                            "Mot de passe temporaire : %s\n\n" +
                            "IMPORTANT : Vous devrez changer ce mot de passe lors de votre première connexion.\n\n" +
                            "Cordialement,\nL'équipe ASTBA",
                    prenom, nom, to, tempPassword
            );
            sendSimpleEmail(to, subject, simpleBody);
        }
    }
}