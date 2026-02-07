package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.astba.dto.InscriptionCreateRequest;
import tn.esprit.astba.dto.InscriptionResponse;
import tn.esprit.astba.entity.*;
import tn.esprit.astba.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final PresenceRepository presenceRepository;

    // ============ CRÉER UNE INSCRIPTION ============
    @Transactional
    public InscriptionResponse createInscription(InscriptionCreateRequest request) {
        // Vérifier que l'élève existe et a le bon rôle
        User eleve = userRepository.findById(request.getEleveId())
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

        if (eleve.getRole() != User.Role.ELEVE) {
            throw new RuntimeException("L'utilisateur sélectionné n'est pas un élève");
        }

        if (!eleve.getStatus()) {
            throw new RuntimeException("L'élève n'est pas actif");
        }

        // Vérifier que la formation existe
        Formation formation = formationRepository.findById(request.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // Vérifier que l'élève n'est pas déjà inscrit à cette formation
        if (inscriptionRepository.existsByEleveIdAndFormationId(request.getEleveId(), request.getFormationId())) {
            throw new RuntimeException("L'élève est déjà inscrit à cette formation");
        }

        Inscription inscription = new Inscription();
        inscription.setEleve(eleve);
        inscription.setFormation(formation);
        inscription.setDateInscription(LocalDate.now());
        inscription.setStatut(Inscription.Statut.EN_COURS);

        Inscription savedInscription = inscriptionRepository.save(inscription);
        return convertToInscriptionResponse(savedInscription);
    }

    // ============ RÉCUPÉRER TOUTES LES INSCRIPTIONS ============
    public List<InscriptionResponse> getAllInscriptions() {
        return inscriptionRepository.findAll().stream()
                .map(this::convertToInscriptionResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER UNE INSCRIPTION PAR ID ============
    public InscriptionResponse getInscriptionById(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
        return convertToInscriptionResponse(inscription);
    }

    // ============ RÉCUPÉRER LES INSCRIPTIONS D'UN ÉLÈVE ============
    public List<InscriptionResponse> getInscriptionsByEleve(Long eleveId) {
        // Vérifier que l'élève existe
        User eleve = userRepository.findById(eleveId)
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

        if (eleve.getRole() != User.Role.ELEVE) {
            throw new RuntimeException("L'utilisateur n'est pas un élève");
        }

        return inscriptionRepository.findByEleveId(eleveId).stream()
                .map(this::convertToInscriptionResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER LES INSCRIPTIONS D'UNE FORMATION ============
    public List<InscriptionResponse> getInscriptionsByFormation(Long formationId) {
        // Vérifier que la formation existe
        formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        return inscriptionRepository.findByFormationId(formationId).stream()
                .map(this::convertToInscriptionResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER PAR STATUT ============
    public List<InscriptionResponse> getInscriptionsByStatut(Inscription.Statut statut) {
        return inscriptionRepository.findByStatut(statut).stream()
                .map(this::convertToInscriptionResponse)
                .collect(Collectors.toList());
    }

    // ============ TERMINER UNE INSCRIPTION ============
    @Transactional
    public InscriptionResponse terminerInscription(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

        if (inscription.getStatut() == Inscription.Statut.TERMINEE) {
            throw new RuntimeException("Cette inscription est déjà terminée");
        }

        // Vérifier que l'élève a validé tous les niveaux (4 niveaux)
        Formation formation = inscription.getFormation();
        if (formation.getNiveaux() == null || formation.getNiveaux().size() < 4) {
            throw new RuntimeException("La formation ne possède pas encore les 4 niveaux requis");
        }

        // Vérifier les présences pour chaque niveau
        for (Niveau niveau : formation.getNiveaux()) {
            if (niveau.getSeances() == null || niveau.getSeances().size() < 6) {
                throw new RuntimeException(String.format("Le niveau %d ne possède pas encore les 6 séances requises", niveau.getNumeroOrdre()));
            }

            // Compter les présences de l'élève pour ce niveau
            long presencesCount = niveau.getSeances().stream()
                    .filter(seance -> presenceRepository.existsByEleveIdAndSeanceIdAndPresent(
                            inscription.getEleve().getId(),
                            seance.getId(),
                            true))
                    .count();

            // L'élève doit avoir assisté à toutes les séances
            if (presencesCount < 6) {
                throw new RuntimeException(String.format("L'élève n'a pas assisté à toutes les séances du niveau %d", niveau.getNumeroOrdre()));
            }
        }

        inscription.setStatut(Inscription.Statut.TERMINEE);
        Inscription updatedInscription = inscriptionRepository.save(inscription);

        return convertToInscriptionResponse(updatedInscription);
    }

    // ============ SUPPRIMER UNE INSCRIPTION ============
    @Transactional
    public String deleteInscription(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

        String eleveNom = inscription.getEleve().getPrenom() + " " + inscription.getEleve().getNom();
        String formationNom = inscription.getFormation().getNom();

        inscriptionRepository.delete(inscription);

        return String.format("Inscription de %s à la formation '%s' supprimée avec succès", eleveNom, formationNom);
    }

    // ============ STATISTIQUES ============
    public InscriptionStatsResponse getStatistics() {
        long totalInscriptions = inscriptionRepository.count();
        long inscriptionsEnCours = inscriptionRepository.findByStatut(Inscription.Statut.EN_COURS).size();
        long inscriptionsTerminees = inscriptionRepository.findByStatut(Inscription.Statut.TERMINEE).size();

        return new InscriptionStatsResponse(
                totalInscriptions,
                inscriptionsEnCours,
                inscriptionsTerminees
        );
    }

    // ============ MÉTHODE UTILITAIRE ============
    private InscriptionResponse convertToInscriptionResponse(Inscription inscription) {
        InscriptionResponse response = new InscriptionResponse();
        response.setId(inscription.getId());

        // Informations de l'élève
        response.setEleveId(inscription.getEleve().getId());
        response.setEleveNom(inscription.getEleve().getNom());
        response.setElevePrenom(inscription.getEleve().getPrenom());
        response.setEleveEmail(inscription.getEleve().getEmail());

        // Informations de la formation
        response.setFormationId(inscription.getFormation().getId());
        response.setFormationNom(inscription.getFormation().getNom());
        response.setFormationDescription(inscription.getFormation().getDescription());

        response.setDateInscription(inscription.getDateInscription());
        response.setStatut(inscription.getStatut());

        // Progression
        Formation formation = inscription.getFormation();
        int nombreNiveaux = formation.getNiveaux() != null ? formation.getNiveaux().size() : 0;

        int nombreSeancesTotal = 0;
        if (formation.getNiveaux() != null) {
            nombreSeancesTotal = formation.getNiveaux().stream()
                    .mapToInt(n -> n.getSeances() != null ? n.getSeances().size() : 0)
                    .sum();
        }

        // Compter les présences de l'élève
        long nombrePresences = presenceRepository.countByEleveIdAndPresent(inscription.getEleve().getId(), true);

        double tauxPresence = nombreSeancesTotal > 0
                ? (double) nombrePresences / nombreSeancesTotal * 100
                : 0;

        response.setNombreNiveauxTotal(nombreNiveaux);
        response.setNombreSeancesTotal(nombreSeancesTotal);
        response.setNombrePresences((int) nombrePresences);
        response.setTauxPresence(Math.round(tauxPresence * 100.0) / 100.0);

        return response;
    }

    // ============ DTO POUR LES STATISTIQUES ============
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class InscriptionStatsResponse {
        private long totalInscriptions;
        private long inscriptionsEnCours;
        private long inscriptionsTerminees;
    }
}