package ru.ianedw.avitoparser.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ianedw.avitoparser.models.Post;
import ru.ianedw.avitoparser.parser.Parser;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostsController {
    private final Parser parser;

    @Autowired
    public PostsController(Parser parser) {
        this.parser = parser;
    }

    @GetMapping
    public Map<Integer, List<Post>> getUpdate(@RequestBody List<Integer> ids) {
        return parser.getLastPosts(ids);
    }
}
