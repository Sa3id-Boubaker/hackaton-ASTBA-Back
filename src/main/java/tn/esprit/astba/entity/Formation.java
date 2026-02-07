package tn.esprit.astba.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "formations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 1000)
    private String description;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    @ManyToOne
    @JoinColumn(name = "formateur_id")
    private User formateur;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    private List<Niveau> niveaux;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    private List<Inscription> inscriptions;
}