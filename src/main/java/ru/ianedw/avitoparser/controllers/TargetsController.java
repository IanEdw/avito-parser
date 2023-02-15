package ru.ianedw.avitoparser.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ianedw.avitoparser.models.Target;
import ru.ianedw.avitoparser.services.TargetsService;

import java.util.List;

@RestController
@RequestMapping("/targets")
public class TargetsController {
    private final TargetsService targetsService;
    Logger log = LoggerFactory.getLogger(TargetsController.class);

    @Autowired
    public TargetsController(TargetsService targetsService) {
        this.targetsService = targetsService;
    }

    @GetMapping
    public List<Target> getAllLinks() {
        return targetsService.getAll();
    }

    @PostMapping
    public Target createTarget(@RequestBody Target postedTarget) {
        String link = postedTarget.getLink();
        if (link.contains("https://www.avito.ru")) {
            Target target = targetsService.findOneByLink(link);
            if (target == null) {
                return targetsService.save(postedTarget);
            } else {
                return target;
            }
        } else {
            return null;
        }
    }

    @DeleteMapping
    public void deleteTarget(@RequestParam int id) {
        targetsService.deleteTargetById(id);
    }
}
