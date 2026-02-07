package tn.esprit.astba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.astba.entity.Niveau;

import java.util.List;
import java.util.Optional;

@Repository
public interface NiveauRepository extends JpaRepository<Niveau, Long> {
    List<Niveau> findByFormationIdOrderByNumeroOrdreAsc(Long formationId);
    Optional<Niveau> findByFormationIdAndNumeroOrdre(Long formationId, Integer numeroOrdre);
    boolean existsByFormationIdAndNumeroOrdre(Long formationId, Integer numeroOrdre);
    long countByFormationId(Long formationId);
}