package ru.ianedw.avitoparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ianedw.avitoparser.models.Target;

import java.util.Optional;

@Repository
public interface TargetsRepository extends JpaRepository<Target, Integer> {
    Optional<Target> findTargetLinkByLink(String link);
    void deleteTargetByLink(String link);
}