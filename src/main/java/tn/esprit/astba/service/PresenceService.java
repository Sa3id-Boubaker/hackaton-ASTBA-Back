package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.astba.dto.*;
import tn.esprit.astba.entity.*;
import tn.esprit.astba.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final PresenceRepository presenceRepository;
    private final UserRepository userRepository;
    private final SeanceRepository seanceRepository;
    private final InscriptionRepository inscriptionRepository;

    // ============ MARQUER UNE PRÉSENCE ============
    @Transactional
    public PresenceResponse marquerPresence(PresenceCreateRequest request) {
        // Vérifier que l'élève existe
        User eleve = userRepository.findById(request.getEleveId())
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

        if (eleve.getRole() != User.Role.ELEVE) {
            throw new RuntimeException("L'utilisateur n'est pas un élève");
        }

        // Vérifier que la séance existe
        Seance seance = seanceRepository.findById(request.getSeanceId())
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        // Vérifier que l'élève est inscrit à la formation
        Formation formation = seance.getNiveau().getFormation();
        if (!inscriptionRepository.existsByEleveIdAndFormationId(request.getEleveId(), formation.getId())) {
            throw new RuntimeException("L'élève n'est pas inscrit à cette formation");
        }

        // Vérifier si une présence existe déjà
        Presence existingPresence = presenceRepository.findByEleveIdAndSeanceId(
                request.getEleveId(),
                request.getSeanceId()
        ).orElse(null);

        if (existingPresence != null) {
            // Mettre à jour la présence existante
            existingPresence.setPresent(request.getPresent());
            existingPresence.setDateMarquage(LocalDateTime.now());
            Presence updated = presenceRepository.save(existingPresence);
            return convertToPresenceResponse(updated);
        }

        // Créer une nouvelle présence
        Presence presence = new Presence();
        presence.setEleve(eleve);
        presence.setSeance(seance);
        presence.setPresent(request.getPresent() != null ? request.getPresent() : false);
        presence.setDateMarquage(LocalDateTime.now());

        Presence savedPresence = presenceRepository.save(presence);
        return convertToPresenceResponse(savedPresence);
    }

    // ============ MARQUER PLUSIEURS PRÉSENCES (BULK) ============
    @Transactional
    public List<PresenceResponse> marquerPresencesBulk(PresenceBulkRequest request) {
        // Vérifier que la séance existe
        Seance seance = seanceRepository.findById(request.getSeanceId())
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        List<PresenceResponse> responses = new ArrayList<>();

        for (PresenceBulkRequest.ElevePresence elevePresence : request.getPresences()) {
            try {
                PresenceCreateRequest createRequest = new PresenceCreateRequest();
                createRequest.setEleveId(elevePresence.getEleveId());
                createRequest.setSeanceId(request.getSeanceId());
                createRequest.setPresent(elevePresence.getPresent());

                PresenceResponse response = marquerPresence(createRequest);
                responses.add(response);
            } catch (RuntimeException e) {
                // Log l'erreur mais continue avec les autres
                System.err.println("Erreur pour l'élève " + elevePresence.getEleveId() + ": " + e.getMessage());
            }
        }

        return responses;
    }

    // ============ MODIFIER UNE PRÉSENCE ============
    @Transactional
    public PresenceResponse updatePresence(Long id, PresenceUpdateRequest request) {
        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Présence non trouvée"));

        if (request.getPresent() != null) {
            presence.setPresent(request.getPresent());
            presence.setDateMarquage(LocalDateTime.now());
        }

        Presence updatedPresence = presenceRepository.save(presence);
        return convertToPresenceResponse(updatedPresence);
    }

    // ============ RÉCUPÉRER TOUTES LES PRÉSENCES ============
    public List<PresenceResponse> getAllPresences() {
        return presenceRepository.findAll().stream()
                .map(this::convertToPresenceResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER UNE PRÉSENCE PAR ID ============
    public PresenceResponse getPresenceById(Long id) {
        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Présence non trouvée"));
        return convertToPresenceResponse(presence);
    }

    // ============ RÉCUPÉRER LES PRÉSENCES D'UN ÉLÈVE ============
    public List<PresenceResponse> getPresencesByEleve(Long eleveId) {
        userRepository.findById(eleveId)
                .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

        return presenceRepository.findByEleveId(eleveId).stream()
                .map(this::convertToPresenceResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER LES PRÉSENCES D'UNE SÉANCE ============
    public List<PresenceResponse> getPresencesBySeance(Long seanceId) {
        seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        return presenceRepository.findBySeanceId(seanceId).stream()
                .map(this::convertToPresenceResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER LES PRÉSENCES D'UN ÉLÈVE POUR UNE FORMATION ============
    public List<PresenceResponse> getPresencesByEleveAndFormation(Long eleveId, Long formationId) {
        return presenceRepository.findByEleveIdAndFormationId(eleveId, formationId).stream()
                .map(this::convertToPresenceResponse)
                .collect(Collectors.toList());
    }

    // ============ STATISTIQUES DES PRÉSENCES D'UNE SÉANCE ============
    public PresenceSeanceStatsResponse getSeanceStats(Long seanceId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        long nombrePresents = presenceRepository.countBySeanceIdAndPresent(seanceId, true);
        long nombreAbsents = presenceRepository.countBySeanceIdAndPresent(seanceId, false);
        long totalInscrits = inscriptionRepository.countByFormationId(
                seance.getNiveau().getFormation().getId()
        );

        return new PresenceSeanceStatsResponse(
                seanceId,
                seance.getTitre(),
                nombrePresents,
                nombreAbsents,
                totalInscrits
        );
    }

    // ============ SUPPRIMER UNE PRÉSENCE ============
    @Transactional
    public String deletePresence(Long id) {
        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Présence non trouvée"));

        presenceRepository.delete(presence);
        return "Présence supprimée avec succès";
    }

    // ============ MÉTHODE UTILITAIRE ============
    private PresenceResponse convertToPresenceResponse(Presence presence) {
        PresenceResponse response = new PresenceResponse();
        response.setId(presence.getId());

        // Informations de l'élève
        response.setEleveId(presence.getEleve().getId());
        response.setEleveNom(presence.getEleve().getNom());
        response.setElevePrenom(presence.getEleve().getPrenom());
        response.setEleveEmail(presence.getEleve().getEmail());

        // Informations de la séance
        response.setSeanceId(presence.getSeance().getId());
        response.setSeanceTitre(presence.getSeance().getTitre());
        response.setSeanceNumeroOrdre(presence.getSeance().getNumeroOrdre());

        // Informations du niveau
        response.setNiveauId(presence.getSeance().getNiveau().getId());
        response.setNiveauNom(presence.getSeance().getNiveau().getNom());
        response.setNiveauNumeroOrdre(presence.getSeance().getNiveau().getNumeroOrdre());

        // Informations de la formation
        response.setFormationId(presence.getSeance().getNiveau().getFormation().getId());
        response.setFormationNom(presence.getSeance().getNiveau().getFormation().getNom());

        response.setPresent(presence.getPresent());
        response.setDateMarquage(presence.getDateMarquage());

        return response;
    }

    // ============ DTO POUR LES STATISTIQUES ============
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PresenceSeanceStatsResponse {
        private Long seanceId;
        private String seanceTitre;
        private long nombrePresents;
        private long nombreAbsents;
        private long totalInscrits;
    }
}