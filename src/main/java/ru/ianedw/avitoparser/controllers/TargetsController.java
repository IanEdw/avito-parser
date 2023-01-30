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
    private final TargetsService linkService;
    Logger log = LoggerFactory.getLogger(TargetsController.class);

    @Autowired
    public TargetsController(TargetsService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public List<Target> getAllLinks() {
        return linkService.findAll();
    }

    @PostMapping
    public Target createTarget(@RequestBody Target postedTarget) {
        String link = postedTarget.getLink();
        if (link.contains("https://www.avito.ru")) {
            Target target = linkService.findOneByLink(link);
            if (target == null) {
                linkService.save(postedTarget);
                return linkService.findOneByLink(link);
            } else {
                return target;
            }
        } else {
            return null;
        }
    }
}
