package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.astba.dto.FormationCreateRequest;
import tn.esprit.astba.dto.FormationResponse;
import tn.esprit.astba.dto.FormationUpdateRequest;
import tn.esprit.astba.entity.Formation;
import tn.esprit.astba.entity.User;
import tn.esprit.astba.repository.FormationRepository;
import tn.esprit.astba.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final UserRepository userRepository;

    // ============ CRÉER UNE FORMATION ============
    @Transactional
    public FormationResponse createFormation(FormationCreateRequest request) {
        // Vérifier si le nom existe déjà
        if (formationRepository.existsByNom(request.getNom())) {
            throw new RuntimeException("Une formation avec ce nom existe déjà");
        }

        // Vérifier que le formateur existe et a le bon rôle
        User formateur = null;
        if (request.getFormateurId() != null) {
            formateur = userRepository.findById(request.getFormateurId())
                    .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

            if (formateur.getRole() != User.Role.FORMATEUR) {
                throw new RuntimeException("L'utilisateur sélectionné n'est pas un formateur");
            }

            if (!formateur.getStatus()) {
                throw new RuntimeException("Le formateur sélectionné n'est pas actif");
            }
        }

        // Valider les dates
        if (request.getDateDebut() != null && request.getDateFin() != null) {
            if (request.getDateFin().isBefore(request.getDateDebut())) {
                throw new RuntimeException("La date de fin doit être après la date de début");
            }
        }

        Formation formation = new Formation();
        formation.setNom(request.getNom());
        formation.setDescription(request.getDescription());
        formation.setDateDebut(request.getDateDebut());
        formation.setDateFin(request.getDateFin());
        formation.setFormateur(formateur);

        Formation savedFormation = formationRepository.save(formation);

        return convertToFormationResponse(savedFormation);
    }

    // ============ MODIFIER UNE FORMATION ============
    @Transactional
    public FormationResponse updateFormation(Long id, FormationUpdateRequest request) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // Vérifier si le nouveau nom existe déjà (pour une autre formation)
        if (request.getNom() != null && !formation.getNom().equals(request.getNom())) {
            if (formationRepository.existsByNomAndIdNot(request.getNom(), id)) {
                throw new RuntimeException("Une formation avec ce nom existe déjà");
            }
            formation.setNom(request.getNom());
        }

        // Mettre à jour uniquement les champs fournis (non null)
        if (request.getDescription() != null) {
            formation.setDescription(request.getDescription());
        }

        if (request.getDateDebut() != null) {
            formation.setDateDebut(request.getDateDebut());
        }

        if (request.getDateFin() != null) {
            formation.setDateFin(request.getDateFin());
        }

        // Valider les dates seulement si les deux sont présentes
        if (formation.getDateDebut() != null && formation.getDateFin() != null) {
            if (formation.getDateFin().isBefore(formation.getDateDebut())) {
                throw new RuntimeException("La date de fin doit être après la date de début");
            }
        }

        // ✅ Mettre à jour le formateur SEULEMENT si formateurId est fourni
        if (request.getFormateurId() != null) {
            User formateur = userRepository.findById(request.getFormateurId())
                    .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

            if (formateur.getRole() != User.Role.FORMATEUR) {
                throw new RuntimeException("L'utilisateur sélectionné n'est pas un formateur");
            }

            if (!formateur.getStatus()) {
                throw new RuntimeException("Le formateur sélectionné n'est pas actif");
            }

            formation.setFormateur(formateur);
        }
        // ✅ Si formateurId n'est pas fourni, on garde l'ancien formateur

        Formation updatedFormation = formationRepository.save(formation);

        return convertToFormationResponse(updatedFormation);
    }

    // ============ RÉCUPÉRER TOUTES LES FORMATIONS ============
    public List<FormationResponse> getAllFormations() {
        return formationRepository.findAll().stream()
                .map(this::convertToFormationResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER UNE FORMATION PAR ID ============
    public FormationResponse getFormationById(Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
        return convertToFormationResponse(formation);
    }

    // ============ RECHERCHER PAR NOM ============
    public List<FormationResponse> searchFormationsByNom(String nom) {
        return formationRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(this::convertToFormationResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER LES FORMATIONS D'UN FORMATEUR ============
    public List<FormationResponse> getFormationsByFormateur(Long formateurId) {
        // Vérifier que le formateur existe
        User formateur = userRepository.findById(formateurId)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        if (formateur.getRole() != User.Role.FORMATEUR) {
            throw new RuntimeException("L'utilisateur n'est pas un formateur");
        }

        return formationRepository.findByFormateurId(formateurId).stream()
                .map(this::convertToFormationResponse)
                .collect(Collectors.toList());
    }

    // ============ SUPPRIMER UNE FORMATION ============
    @Transactional
    public String deleteFormation(Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // Vérifier s'il y a des inscriptions
        if (formation.getInscriptions() != null && !formation.getInscriptions().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer une formation avec des inscriptions actives");
        }

        String nomFormation = formation.getNom();
        formationRepository.delete(formation);

        return String.format("Formation '%s' supprimée avec succès", nomFormation);
    }

    // ============ ASSIGNER/CHANGER LE FORMATEUR ============
    @Transactional
    public FormationResponse assignFormateur(Long formationId, Long formateurId) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        User formateur = userRepository.findById(formateurId)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        if (formateur.getRole() != User.Role.FORMATEUR) {
            throw new RuntimeException("L'utilisateur sélectionné n'est pas un formateur");
        }

        if (!formateur.getStatus()) {
            throw new RuntimeException("Le formateur sélectionné n'est pas actif");
        }

        formation.setFormateur(formateur);
        Formation updatedFormation = formationRepository.save(formation);

        return convertToFormationResponse(updatedFormation);
    }

    // ============ RETIRER LE FORMATEUR ============
    @Transactional
    public FormationResponse removeFormateur(Long formationId) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        formation.setFormateur(null);
        Formation updatedFormation = formationRepository.save(formation);

        return convertToFormationResponse(updatedFormation);
    }

    // ============ STATISTIQUES ============
    public FormationStatsResponse getStatistics() {
        long totalFormations = formationRepository.count();
        long formationsAvecFormateur = formationRepository.findAll().stream()
                .filter(f -> f.getFormateur() != null)
                .count();
        long formationsSansFormateur = totalFormations - formationsAvecFormateur;

        return new FormationStatsResponse(
                totalFormations,
                formationsAvecFormateur,
                formationsSansFormateur
        );
    }

    // ============ MÉTHODE UTILITAIRE ============
    private FormationResponse convertToFormationResponse(Formation formation) {
        FormationResponse response = new FormationResponse();
        response.setId(formation.getId());
        response.setNom(formation.getNom());
        response.setDescription(formation.getDescription());
        response.setDateDebut(formation.getDateDebut());
        response.setDateFin(formation.getDateFin());

        // Informations du formateur
        if (formation.getFormateur() != null) {
            response.setFormateurId(formation.getFormateur().getId());
            response.setFormateurNom(formation.getFormateur().getNom());
            response.setFormateurPrenom(formation.getFormateur().getPrenom());
            response.setFormateurEmail(formation.getFormateur().getEmail());
        }

        // Statistiques
        response.setNombreNiveaux(formation.getNiveaux() != null ? formation.getNiveaux().size() : 0);
        response.setNombreInscrits(formation.getInscriptions() != null ? formation.getInscriptions().size() : 0);

        return response;
    }

    // ============ DTO POUR LES STATISTIQUES ============
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class FormationStatsResponse {
        private long totalFormations;
        private long formationsAvecFormateur;
        private long formationsSansFormateur;
    }
}