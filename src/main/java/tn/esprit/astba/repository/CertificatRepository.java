package tn.esprit.astba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.astba.entity.Certificat;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificatRepository extends JpaRepository<Certificat, Long> {
    // Vérifier si un certificat existe déjà
    boolean existsByEleveIdAndFormationId(Long eleveId, Long formationId);

    Optional<Certificat> findByEleveIdAndFormationId(Long eleveId, Long formationId);

    Optional<Certificat> findByNumeroCertificat(String numeroCertificat);

    // Récupérer les certificats d'un élève
    List<Certificat> findByEleveId(Long eleveId);

    // Récupérer les certificats d'une formation
    List<Certificat> findByFormationId(Long formationId);
}