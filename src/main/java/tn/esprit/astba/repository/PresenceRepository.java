package tn.esprit.astba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.astba.entity.Presence;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {
    // Vérifier si une présence existe déjà
    boolean existsByEleveIdAndSeanceId(Long eleveId, Long seanceId);

    Optional<Presence> findByEleveIdAndSeanceId(Long eleveId, Long seanceId);

    // Récupérer les présences d'un élève
    List<Presence> findByEleveId(Long eleveId);

    // Récupérer les présences d'une séance
    List<Presence> findBySeanceId(Long seanceId);

    // Récupérer les présences d'un élève pour une formation
    @Query("SELECT p FROM Presence p WHERE p.eleve.id = :eleveId AND p.seance.niveau.formation.id = :formationId")
    List<Presence> findByEleveIdAndFormationId(Long eleveId, Long formationId);

    // Compter les présences
    boolean existsByEleveIdAndSeanceIdAndPresent(Long eleveId, Long seanceId, Boolean present);
    long countByEleveIdAndPresent(Long eleveId, Boolean present);

    // Compter les présences pour une séance
    long countBySeanceIdAndPresent(Long seanceId, Boolean present);
}