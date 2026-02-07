package tn.esprit.astba.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.astba.dto.NiveauCreateRequest;
import tn.esprit.astba.dto.NiveauResponse;
import tn.esprit.astba.dto.NiveauUpdateRequest;
import tn.esprit.astba.entity.Formation;
import tn.esprit.astba.entity.Niveau;
import tn.esprit.astba.repository.FormationRepository;
import tn.esprit.astba.repository.NiveauRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NiveauService {

    private final NiveauRepository niveauRepository;
    private final FormationRepository formationRepository;

    // ============ CRÉER UN NIVEAU ============
    @Transactional
    public NiveauResponse createNiveau(NiveauCreateRequest request) {
        // Vérifier que la formation existe
        Formation formation = formationRepository.findById(request.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // Valider le numéro d'ordre (1 à 4)
        if (request.getNumeroOrdre() < 1 || request.getNumeroOrdre() > 4) {
            throw new RuntimeException("Le numéro d'ordre doit être entre 1 et 4");
        }

        // Vérifier que le numéro d'ordre n'existe pas déjà pour cette formation
        if (niveauRepository.existsByFormationIdAndNumeroOrdre(request.getFormationId(), request.getNumeroOrdre())) {
            throw new RuntimeException("Un niveau avec ce numéro d'ordre existe déjà pour cette formation");
        }

        // Vérifier qu'il n'y a pas plus de 4 niveaux
        long count = niveauRepository.countByFormationId(request.getFormationId());
        if (count >= 4) {
            throw new RuntimeException("Une formation ne peut pas avoir plus de 4 niveaux");
        }

        Niveau niveau = new Niveau();
        niveau.setNom(request.getNom());
        niveau.setNumeroOrdre(request.getNumeroOrdre());
        niveau.setFormation(formation);

        Niveau savedNiveau = niveauRepository.save(niveau);
        return convertToNiveauResponse(savedNiveau);
    }

    // ============ MODIFIER UN NIVEAU ============
    @Transactional
    public NiveauResponse updateNiveau(Long id, NiveauUpdateRequest request) {
        Niveau niveau = niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));

        // Mettre à jour le nom si fourni
        if (request.getNom() != null) {
            niveau.setNom(request.getNom());
        }

        // Mettre à jour le numéro d'ordre si fourni
        if (request.getNumeroOrdre() != null) {
            // Valider le numéro d'ordre
            if (request.getNumeroOrdre() < 1 || request.getNumeroOrdre() > 4) {
                throw new RuntimeException("Le numéro d'ordre doit être entre 1 et 4");
            }

            // Vérifier que le nouveau numéro n'existe pas déjà (sauf pour ce niveau)
            Niveau existing = niveauRepository.findByFormationIdAndNumeroOrdre(
                    niveau.getFormation().getId(),
                    request.getNumeroOrdre()
            ).orElse(null);

            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("Un niveau avec ce numéro d'ordre existe déjà pour cette formation");
            }

            niveau.setNumeroOrdre(request.getNumeroOrdre());
        }

        Niveau updatedNiveau = niveauRepository.save(niveau);
        return convertToNiveauResponse(updatedNiveau);
    }

    // ============ RÉCUPÉRER TOUS LES NIVEAUX ============
    public List<NiveauResponse> getAllNiveaux() {
        return niveauRepository.findAll().stream()
                .map(this::convertToNiveauResponse)
                .collect(Collectors.toList());
    }

    // ============ RÉCUPÉRER UN NIVEAU PAR ID ============
    public NiveauResponse getNiveauById(Long id) {
        Niveau niveau = niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));
        return convertToNiveauResponse(niveau);
    }

    // ============ RÉCUPÉRER LES NIVEAUX D'UNE FORMATION ============
    public List<NiveauResponse> getNiveauxByFormation(Long formationId) {
        // Vérifier que la formation existe
        formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        return niveauRepository.findByFormationIdOrderByNumeroOrdreAsc(formationId).stream()
                .map(this::convertToNiveauResponse)
                .collect(Collectors.toList());
    }

    // ============ SUPPRIMER UN NIVEAU ============
    @Transactional
    public String deleteNiveau(Long id) {
        Niveau niveau = niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));

        // Vérifier s'il y a des séances
        if (niveau.getSeances() != null && !niveau.getSeances().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un niveau avec des séances");
        }

        String nomNiveau = niveau.getNom();
        niveauRepository.delete(niveau);

        return String.format("Niveau '%s' supprimé avec succès", nomNiveau);
    }

    // ============ MÉTHODE UTILITAIRE ============
    private NiveauResponse convertToNiveauResponse(Niveau niveau) {
        NiveauResponse response = new NiveauResponse();
        response.setId(niveau.getId());
        response.setNom(niveau.getNom());
        response.setNumeroOrdre(niveau.getNumeroOrdre());
        response.setFormationId(niveau.getFormation().getId());
        response.setFormationNom(niveau.getFormation().getNom());
        response.setNombreSeances(niveau.getSeances() != null ? niveau.getSeances().size() : 0);
        return response;
    }
}