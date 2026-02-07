package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.astba.dto.SeanceCreateRequest;
import tn.esprit.astba.dto.SeanceResponse;
import tn.esprit.astba.dto.SeanceUpdateRequest;
import tn.esprit.astba.entity.Niveau;
import tn.esprit.astba.entity.Seance;
import tn.esprit.astba.repository.NiveauRepository;
import tn.esprit.astba.repository.SeanceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final NiveauRepository niveauRepository;

    // ============ CRÉER UNE SÉANCE ============
    @Transactional
    public SeanceResponse createSeance(SeanceCreateRequest request) {
        // Vérifier que le niveau existe
        Niveau niveau = niveauRepository.findById(request.getNiveauId())
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));

        // Valider le numéro d'ordre (1 à 6)
        if (request.getNumeroOrdre() < 1 || request.getNumeroOrdre() > 6) {
            throw new RuntimeException("Le numéro d'ordre doit être entre 1 et 6");
        }

        // Vérifier que le numéro d'ordre n'existe pas déjà pour ce niveau
        if (seanceRepository.existsByNiveauIdAndNumeroOrdre(request.getNiveauId(), request.getNumeroOrdre())) {
            throw new RuntimeException("Une séance avec ce numéro d'ordre existe déjà pour ce niveau");
        }

        // Vérifier qu'il n'y a pas plus de 6 séances
        long count = seanceRepository.countByNiveauId(request.getNiveauId());
        if (count >= 6) {
            throw new RuntimeException("Un niveau ne peut pas avoir plus de 6 séances");
        }

        // Valider les heures
        if (request.getHeureDebut() != null && request.getHeureFin() != null) {
            if (request.getHeureFin().isBefore(request.getHeureDebut())) {
                throw new RuntimeException("L'heure de fin doit être après l'heure de début");
            }
        }

        Seance seance = new Seance();
        seance.setTitre(request.getTitre());
        seance.setDescription(request.getDescription());
        seance.setDateSeance(request.getDateSeance());
        seance.setHeureDebut(request.getHeureDebut());
        seance.setHeureFin(request.getHeureFin());
        seance.setNumeroOrdre(request.getNumeroOrdre());
        seance.setNiveau(niveau);

        Seance savedSeance = seanceRepository.save(seance);
        return convertToSeanceResponse(savedSeance);
    }

    // ============ MODIFIER UNE SÉANCE ============
    @Transactional
    public SeanceResponse updateSeance(Long id, SeanceUpdateRequest request) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        // Mettre à jour les champs fournis
        if (request.getTitre() != null) {
            seance.setTitre(request.getTitre());
        }

        if (request.getDescription() != null) {
            seance.setDescription(request.getDescription());
        }

        if (request.getDateSeance() != null) {
            seance.setDateSeance(request.getDateSeance());
        }

        if (request.getHeureDebut() != null) {
            seance.setHeureDebut(request.getHeureDebut());
        }

        if (request.getHeureFin() != null) {
            seance.setHeureFin(request.getHeureFin());
        }

        // Valider les heures
        if (seance.getHeureDebut() != null && seance.getHeureFin() != null) {
            if (seance.getHeureFin().isBefore(seance.getHeureDebut())) {
                throw new RuntimeException("L'heure de fin doit être après l'heure de début");
            }
        }

        // Mettre à jour le numéro d'ordre si fourni
        if (request.getNumeroOrdre() != null) {
            if (request.getNumeroOrdre() < 1 || request.getNumeroOrdre() > 6) {
                throw new RuntimeException("Le numéro d'ordre doit être entre 1 et 6");
            }

            Seance existing = seanceRepository.findByNiveauIdAndNumeroOrdre(
                    seance.getNiveau().getId(),
                    request.getNumeroOrdre()
            ).orElse(null);

            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("Une séance avec ce numéro d'ordre existe déjà pour ce niveau");
            }

            seance.setNumeroOrdre(request.getNumeroOrdre());
        }

        Seance updatedSeance = seanceRepository.save(seance);
        return convertToSeanceResponse(updatedSeance);
    }

    // ============ RÉCUPÉRER TOUTES LES SÉANCES ============
    public List<SeanceResponse> getAllSeances() {
        return seanceRepository.findAll().stream()
                .map(this::convertToSeanceResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER UNE SÉANCE PAR ID ============
    public SeanceResponse getSeanceById(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        return convertToSeanceResponse(seance);
    }

    // ============ RÉCUPÉRER LES SÉANCES D'UN NIVEAU ============
    public List<SeanceResponse> getSeancesByNiveau(Long niveauId) {
        niveauRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));

        return seanceRepository.findByNiveauIdOrderByNumeroOrdreAsc(niveauId).stream()
                .map(this::convertToSeanceResponse)
                .collect(Collectors.toList());
    }

    // ============ SUPPRIMER UNE SÉANCE ============
    @Transactional
    public String deleteSeance(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        // Vérifier s'il y a des présences
        if (seance.getPresences() != null && !seance.getPresences().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer une séance avec des présences enregistrées");
        }

        String titreSeance = seance.getTitre();
        seanceRepository.delete(seance);

        return String.format("Séance '%s' supprimée avec succès", titreSeance);
    }

    // ============ MÉTHODE UTILITAIRE ============
    private SeanceResponse convertToSeanceResponse(Seance seance) {
        SeanceResponse response = new SeanceResponse();
        response.setId(seance.getId());
        response.setTitre(seance.getTitre());
        response.setDescription(seance.getDescription());
        response.setDateSeance(seance.getDateSeance());
        response.setHeureDebut(seance.getHeureDebut());
        response.setHeureFin(seance.getHeureFin());
        response.setNumeroOrdre(seance.getNumeroOrdre());
        response.setNiveauId(seance.getNiveau().getId());
        response.setNiveauNom(seance.getNiveau().getNom());
        response.setNiveauNumeroOrdre(seance.getNiveau().getNumeroOrdre());
        response.setFormationId(seance.getNiveau().getFormation().getId());
        response.setFormationNom(seance.getNiveau().getFormation().getNom());
        response.setNombrePresences(seance.getPresences() != null ? seance.getPresences().size() : 0);
        return response;
    }
}