package ru.ianedw.avitoparser.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ianedw.avitoparser.models.Update;
import ru.ianedw.avitoparser.parser.Parser;

@Service
public class UpdateService {
    private final Parser parser;

    @Autowired
    public UpdateService(Parser parser) {
        this.parser = parser;
    }

    public Update getUpdate() {
        return parser.getUpdate();
    }
}
