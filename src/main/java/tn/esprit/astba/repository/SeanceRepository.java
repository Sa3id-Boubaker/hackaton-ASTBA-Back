package tn.esprit.astba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.astba.entity.Seance;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    List<Seance> findByNiveauIdOrderByNumeroOrdreAsc(Long niveauId);
    Optional<Seance> findByNiveauIdAndNumeroOrdre(Long niveauId, Integer numeroOrdre);
    boolean existsByNiveauIdAndNumeroOrdre(Long niveauId, Integer numeroOrdre);
    long countByNiveauId(Long niveauId);
}