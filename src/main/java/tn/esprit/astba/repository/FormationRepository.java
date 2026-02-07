package tn.esprit.astba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.astba.entity.Formation;

import java.util.List;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {

    // Rechercher par nom (contient)
    List<Formation> findByNomContainingIgnoreCase(String nom);

    // Rechercher par formateur
    List<Formation> findByFormateurId(Long formateurId);

    // Vérifier si le nom existe déjà
    boolean existsByNom(String nom);

    // Vérifier si le nom existe pour un autre ID (pour la modification)
    boolean existsByNomAndIdNot(String nom, Long id);
}