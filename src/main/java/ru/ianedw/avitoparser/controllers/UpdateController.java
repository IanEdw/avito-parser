package ru.ianedw.avitoparser.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ianedw.avitoparser.models.Update;
import ru.ianedw.avitoparser.services.UpdateService;


@RestController
@RequestMapping("/posts")
public class UpdateController {
    private final UpdateService updateService;

    @Autowired
    public UpdateController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @GetMapping
    public Update getUpdate() {
        return updateService.getUpdate();
    }
}
