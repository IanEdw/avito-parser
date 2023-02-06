package ru.ianedw.avitoparser.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ianedw.avitoparser.models.Target;
import ru.ianedw.avitoparser.repositories.TargetsRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TargetsService {
    private final TargetsRepository repository;

    @Autowired
    public TargetsService(TargetsRepository repository) {
        this.repository = repository;
    }

    public List<Target> getAll() {
        return repository.findAll();
    }

    public Target findOneByLink(String link) {
        return repository.findTargetLinkByLink(link).orElse(null);
    }

    @Transactional
    public Target save(Target target) {
        return repository.save(target);
    }

    @Transactional
    public void deleteByLink(String link) {
        repository.deleteTargetByLink(link);
    }
}
