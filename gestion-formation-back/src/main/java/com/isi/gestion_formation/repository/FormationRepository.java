package com.isi.gestion_formation.repository;

import com.isi.gestion_formation.model.Formation;
import com.isi.gestion_formation.repository.iRepository.IFormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FormationRepository {

    private final IFormationRepository formationRepo;

    /**
     * Récupère les formations groupées par année
     * @return List<Object[]> contenant [année, count]
     */
    public List<Object[]> getFormationsParAnnee() {
        try {
            List<Formation> formations = formationRepo.findAll();

            // Grouper par année et compter
            Map<Object, Long> grouped = formations.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getAnnee(),
                            Collectors.counting()
                    ));

            // Convertir en List<Object[]>
            return grouped.entrySet().stream()
                    .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                    .sorted((a, b) -> ((Comparable) a[0]).compareTo(b[0]))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des formations par année : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les formations groupées par domaine
     * @return List<Object[]> contenant [libelle_domaine, count]
     */
    public List<Object[]> getFormationsParDomaine() {
        try {
            List<Formation> formations = formationRepo.findAll();

            // Grouper par domaine et compter
            Map<String, Long> grouped = formations.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getDomaine().getLibelle(),
                            Collectors.counting()
                    ));

            // Convertir en List<Object[]>
            return grouped.entrySet().stream()
                    .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                    .sorted((a, b) -> Long.compare((Long)b[1], (Long)a[1]))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des formations par domaine : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les formations groupées par structure
     * @return List<Object[]> contenant [libelle_structure, count]
     */
    public List<Object[]> getFormationsParStructure() {
        try {
            List<Formation> formations = formationRepo.findAll();

            // Grouper par structure (via participants) et compter les participants distincts
            Map<String, Set<Long>> grouped = new HashMap<>();

            formations.forEach(f -> {
                f.getParticipants().forEach(p -> {
                    String structureLibelle = p.getStructure().getLibelle();
                    grouped.computeIfAbsent(structureLibelle, k -> new HashSet<>())
                            .add(p.getId());
                });
            });

            // Convertir en List<Object[]>
            return grouped.entrySet().stream()
                    .map(entry -> new Object[]{entry.getKey(), (long) entry.getValue().size()})
                    .sorted((a, b) -> Long.compare((Long)b[1], (Long)a[1]))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des formations par structure : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les formations groupées par mois
     * ✅ CORRIGÉ : Utilise dateFormation au lieu de dateDebut
     * @return List<Object[]> contenant [mois-année, count]
     */
    public List<Object[]> getFormationsParMois() {
        try {
            List<Formation> formations = formationRepo.findAll();

            // Grouper par mois-année et compter
            Map<String, Long> grouped = formations.stream()
                    .filter(f -> f.getDateFormation() != null) // ✅ Vérifier que la date existe
                    .collect(Collectors.groupingBy(
                            f -> {
                                int month = f.getDateFormation().getMonthValue();
                                int year = f.getDateFormation().getYear();
                                return String.format("%02d-%d", month, year);
                            },
                            Collectors.counting()
                    ));

            // Convertir en List<Object[]> et trier par date
            return grouped.entrySet().stream()
                    .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                    .sorted((a, b) -> ((String)a[0]).compareTo((String)b[0]))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des formations par mois : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère le nombre total de formations
     * @return Long nombre total
     */
    public Long getTotalFormations() {
        try {
            return formationRepo.count();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du total des formations : " + e.getMessage(), e);
        }
    }
}