package tn.esprit.astba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.astba.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByStatus(Boolean status);
    // Recherche par status
    long countByStatus(Boolean status);

    // Recherche par rôle
    List<User> findByRole(User.Role role);
    long countByRole(User.Role role);
}