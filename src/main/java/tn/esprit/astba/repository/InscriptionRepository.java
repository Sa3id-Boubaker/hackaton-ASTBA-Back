package tn.esprit.astba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.astba.entity.Inscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    // Vérifier si un élève est déjà inscrit à une formation
    boolean existsByEleveIdAndFormationId(Long eleveId, Long formationId);

    Optional<Inscription> findByEleveIdAndFormationId(Long eleveId, Long formationId);

    // Récupérer les inscriptions d'un élève
    List<Inscription> findByEleveId(Long eleveId);

    // Récupérer les inscriptions d'une formation
    List<Inscription> findByFormationId(Long formationId);

    // Récupérer par statut
    List<Inscription> findByStatut(Inscription.Statut statut);

    // Compter les inscriptions par formation
    long countByFormationId(Long formationId);
}