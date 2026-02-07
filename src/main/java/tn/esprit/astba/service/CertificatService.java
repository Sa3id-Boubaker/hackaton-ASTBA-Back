package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.astba.dto.CertificatResponse;
import tn.esprit.astba.entity.*;
import tn.esprit.astba.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificatService {

    private final CertificatRepository certificatRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PresenceRepository presenceRepository;

    // ============ GÉNÉRER UN CERTIFICAT ============
    @Transactional
    public CertificatResponse genererCertificat(Long eleveId, Long formationId) {
        // Vérifier que l'élève existe
        User eleve = userRepository.findById(eleveId)
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

        if (eleve.getRole() != User.Role.ELEVE) {
            throw new RuntimeException("L'utilisateur n'est pas un élève");
        }

        // Vérifier que la formation existe
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // Vérifier qu'un certificat n'existe pas déjà
        if (certificatRepository.existsByEleveIdAndFormationId(eleveId, formationId)) {
            throw new RuntimeException("Un certificat a déjà été généré pour cet élève et cette formation");
        }

        // Vérifier que l'élève est inscrit
        Inscription inscription = inscriptionRepository.findByEleveIdAndFormationId(eleveId, formationId)
                .orElseThrow(() -> new RuntimeException("L'élève n'est pas inscrit à cette formation"));

        // Vérifier que l'inscription est terminée
        if (inscription.getStatut() != Inscription.Statut.TERMINEE) {
            throw new RuntimeException("L'inscription n'est pas encore terminée");
        }

        // Vérifier que la formation a 4 niveaux
        if (formation.getNiveaux() == null || formation.getNiveaux().size() < 4) {
            throw new RuntimeException("La formation doit avoir 4 niveaux pour générer un certificat");
        }

        // Vérifier les présences pour tous les niveaux
        for (Niveau niveau : formation.getNiveaux()) {
            if (niveau.getSeances() == null || niveau.getSeances().size() < 6) {
                throw new RuntimeException(String.format("Le niveau %d doit avoir 6 séances", niveau.getNumeroOrdre()));
            }

            long presencesCount = niveau.getSeances().stream()
                    .filter(seance -> presenceRepository.existsByEleveIdAndSeanceIdAndPresent(
                            eleveId, seance.getId(), true))
                    .count();

            if (presencesCount < 6) {
                throw new RuntimeException(String.format(
                        "L'élève n'a pas assisté à toutes les séances du niveau %d (%d/6)",
                        niveau.getNumeroOrdre(), presencesCount));
            }
        }

        // Générer le numéro de certificat
        String numeroCertificat = genererNumeroCertificat(eleve, formation);

        // Créer le certificat
        Certificat certificat = new Certificat();
        certificat.setEleve(eleve);
        certificat.setFormation(formation);
        certificat.setDateEmission(LocalDate.now());
        certificat.setNumeroCertificat(numeroCertificat);
        certificat.setGenere(true);

        Certificat savedCertificat = certificatRepository.save(certificat);
        return convertToCertificatResponse(savedCertificat);
    }

    // ============ RÉCUPÉRER TOUS LES CERTIFICATS ============
    public List<CertificatResponse> getAllCertificats() {
        return certificatRepository.findAll().stream()
                .map(this::convertToCertificatResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER UN CERTIFICAT PAR ID ============
    public CertificatResponse getCertificatById(Long id) {
        Certificat certificat = certificatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificat non trouvé"));
        return convertToCertificatResponse(certificat);
    }

    // ============ RÉCUPÉRER PAR NUMÉRO ============
    public CertificatResponse getCertificatByNumero(String numeroCertificat) {
        Certificat certificat = certificatRepository.findByNumeroCertificat(numeroCertificat)
                .orElseThrow(() -> new RuntimeException("Certificat non trouvé"));
        return convertToCertificatResponse(certificat);
    }

    // ============ RÉCUPÉRER LES CERTIFICATS D'UN ÉLÈVE ============
    public List<CertificatResponse> getCertificatsByEleve(Long eleveId) {
        userRepository.findById(eleveId)
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

        return certificatRepository.findByEleveId(eleveId).stream()
                .map(this::convertToCertificatResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER LES CERTIFICATS D'UNE FORMATION ============
    public List<CertificatResponse> getCertificatsByFormation(Long formationId) {
        formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        return certificatRepository.findByFormationId(formationId).stream()
                .map(this::convertToCertificatResponse)
                .collect(Collectors.toList());
    }

    // ============ VÉRIFIER L'ÉLIGIBILITÉ ============
    public EligibiliteCertificatResponse verifierEligibilite(Long eleveId, Long formationId) {
        // Vérifier que l'élève existe
        User eleve = userRepository.findById(eleveId)
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

        // Vérifier que la formation existe
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // Vérifier l'inscription
        Inscription inscription = inscriptionRepository.findByEleveIdAndFormationId(eleveId, formationId)
                .orElse(null);

        if (inscription == null) {
            return new EligibiliteCertificatResponse(false, "L'élève n'est pas inscrit à cette formation", null);
        }

        if (inscription.getStatut() != Inscription.Statut.TERMINEE) {
            return new EligibiliteCertificatResponse(false, "L'inscription n'est pas terminée", null);
        }

        // Vérifier les niveaux
        if (formation.getNiveaux() == null || formation.getNiveaux().size() < 4) {
            return new EligibiliteCertificatResponse(false, "La formation n'a pas encore 4 niveaux", null);
        }

        // Vérifier les présences
        for (Niveau niveau : formation.getNiveaux()) {
            if (niveau.getSeances() == null || niveau.getSeances().size() < 6) {
                return new EligibiliteCertificatResponse(
                        false,
                        String.format("Le niveau %d n'a pas 6 séances", niveau.getNumeroOrdre()),
                        null);
            }

            long presencesCount = niveau.getSeances().stream()
                    .filter(seance -> presenceRepository.existsByEleveIdAndSeanceIdAndPresent(
                            eleveId, seance.getId(), true))
                    .count();

            if (presencesCount < 6) {
                return new EligibiliteCertificatResponse(
                        false,
                        String.format("L'élève n'a assisté qu'à %d/6 séances du niveau %d",
                                presencesCount, niveau.getNumeroOrdre()),
                        null);
            }
        }

        // Vérifier si un certificat existe déjà
        if (certificatRepository.existsByEleveIdAndFormationId(eleveId, formationId)) {
            Certificat existingCert = certificatRepository.findByEleveIdAndFormationId(eleveId, formationId).get();
            return new EligibiliteCertificatResponse(
                    true,
                    "Un certificat a déjà été généré",
                    existingCert.getNumeroCertificat());
        }

        return new EligibiliteCertificatResponse(true, "Éligible pour obtenir le certificat", null);
    }

    // ============ SUPPRIMER UN CERTIFICAT ============
    @Transactional
    public String deleteCertificat(Long id) {
        Certificat certificat = certificatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificat non trouvé"));

        String numero = certificat.getNumeroCertificat();
        certificatRepository.delete(certificat);

        return String.format("Certificat %s supprimé avec succès", numero);
    }

    // ============ MÉTHODES UTILITAIRES ============

    private String genererNumeroCertificat(User eleve, Formation formation) {
        // Format: ASTBA-YYYY-FORMATION_ID-ELEVE_ID
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String formationPart = String.format("%04d", formation.getId());
        String elevePart = String.format("%04d", eleve.getId());

        return String.format("ASTBA-%s-%s-%s", year, formationPart, elevePart);
    }

    private CertificatResponse convertToCertificatResponse(Certificat certificat) {
        CertificatResponse response = new CertificatResponse();
        response.setId(certificat.getId());

        // Informations de l'élève
        response.setEleveId(certificat.getEleve().getId());
        response.setEleveNom(certificat.getEleve().getNom());
        response.setElevePrenom(certificat.getEleve().getPrenom());
        response.setEleveEmail(certificat.getEleve().getEmail());

        // Informations de la formation
        response.setFormationId(certificat.getFormation().getId());
        response.setFormationNom(certificat.getFormation().getNom());
        response.setFormationDescription(certificat.getFormation().getDescription());

        response.setDateEmission(certificat.getDateEmission());
        response.setNumeroCertificat(certificat.getNumeroCertificat());
        response.setGenere(certificat.getGenere());

        return response;
    }

    // ============ DTO POUR L'ÉLIGIBILITÉ ============
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class EligibiliteCertificatResponse {
        private Boolean eligible;
        private String message;
        private String numeroCertificatExistant;
    }
}