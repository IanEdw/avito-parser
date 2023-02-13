package ru.ianedw.avitoparser.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.ianedw.avitoparser.models.Update;
import ru.ianedw.avitoparser.models.Post;
import ru.ianedw.avitoparser.models.Target;
import ru.ianedw.avitoparser.services.TargetsService;
import ru.ianedw.avitoparser.util.NotTargetPost;

import java.io.IOException;
import java.util.*;

@Component
public class Parser {
    Logger log = LoggerFactory.getLogger(Parser.class);
    private final TargetsService targetService;
    private final Map<Integer, Map<String, Post>> availablePosts;
    private final String XPATH_QUERY = "//div[@data-marker='catalog-serp']//div[@data-marker='item']";
    private Set<Target> targets;


    @Autowired
    public Parser(TargetsService targetService) {
        this.targetService = targetService;
        availablePosts = new HashMap<>();
        loadPosts(updateTargets());
    }

    public Update getUpdate() {
        Update update = new Update();
        update.setTargetPosts(availablePosts);
        return update;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 15000)
    private void updateAvailablePosts() {
        log.info("Запущено обновление объявлений");
        List<Target> newTargets = updateTargets();
        loadPosts(newTargets);

        for (Target target : targets) {
            log.info("Обновление цели " + target.getId());
            updatePosts(target);
        }
    }

    private void updatePosts(Target target) {
        try {
            Element body = Jsoup.connect(target.getLink()).get().body();

            for (int i = 0; i < body.selectXpath(XPATH_QUERY).size(); i++) {
                try {
                    updatePost(body, i, target.getId());
                } catch (NotTargetPost e) {
                    break;
                }
            }
            log.info("Размер availablePosts для Target " + target.getId() + " = " + availablePosts.get(target.getId()).size());
        } catch (IOException e) {
            log.warn("JSOUP НЕ СМОГ ПОЛУЧИТЬ ДАННЫЕ");
        }
    }

    private void updatePost(Element body, int blockNumber, int targetId) {
        String expression = String.format(XPATH_QUERY + "[%d]", blockNumber + 1);
        String link = findPostLink(body, expression);
        Map<String, Post> targetPosts = availablePosts.get(targetId);

        if (!isPostPublicationTimeValid(body, expression)) {
            targetPosts.remove(link);
            throw new NotTargetPost();
        }

        if (targetPosts.containsKey(link)) {
            return;
        }

        Post post = new Post();

        post.setLink(link);
        post.setName(findPostName(body, expression));
        post.setPrice(findPostPrice(body, expression));

        targetPosts.put(link, post);
    }


    private void loadPosts(List<Target> targets) {
        targets.forEach(this::loadPosts);
    }

    private void loadPosts(Target target) {
        Map<String, Post> posts = new HashMap<>();
        try {
            Element body = Jsoup.connect(target.getLink()).get().body();

            for (int i = 0; i < body.selectXpath(XPATH_QUERY).size(); i++) {
                try {
                    Post post = loadPost(body, i);
                    posts.put(post.getLink(), post);
                } catch (NotTargetPost e) {
                    break;
                }
            }

            availablePosts.put(target.getId(), posts);
            log.info("Загружено " + posts.size() + " объявлений");
        } catch (IOException e) {
            log.warn("JSOUP НЕ СМОГ ПОЛУЧИТЬ ДАННЫЕ");
        }
    }

    private Post loadPost(Element body, int blockNumber) {
        String expression = String.format(XPATH_QUERY + "[%d]", blockNumber + 1);

        if (isPostPublicationTimeValid(body, expression)) {
            Post post = new Post();

            post.setName(findPostName(body, expression));
            post.setLink(findPostLink(body, expression));
            post.setPrice(findPostPrice(body, expression));

            return post;
        } else {
            throw new NotTargetPost();
        }
    }


    private boolean isPostPublicationTimeValid(Element body, String parentExpression) {
        String dateText = body.selectXpath(parentExpression + "//div[@data-marker='item-date']").text();
        String[] dateArray = dateText.split(" ");
        String timeUnit = dateArray[1];

        switch (timeUnit) {
            case "минут", "минуту", "минуты" -> {
                return true;
            }
            case "часов", "час" -> {
                return Integer.parseInt(dateArray[0]) <= 3;
            }
            default -> {
                return false;
            }
        }
    }

    private String findPostName(Element body, String parentExpression) {
        return body.selectXpath(parentExpression + "//a[@data-marker='item-title']").attr("title");
    }

    private String findPostLink(Element body, String parentExpression) {
        return "https://www.avito.ru" + body.selectXpath(parentExpression + "//a[@data-marker='item-title']").attr("href");
    }

    private int findPostPrice(Element body, String parentExpression) {
        return Integer.parseInt(body.selectXpath(parentExpression + "//meta[@itemprop='price']").attr("content"));
    }


    private List<Target> updateTargets() {
        List<Target> updatedList = targetService.getAll();
        if (targets == null) {
            targets = new HashSet<>(updatedList);
            return updatedList;
        }

        List<Target> newTargets = new ArrayList<>();
        for (Target target : updatedList) {
            if (!targets.contains(target)) {
                targets.add(target);
                newTargets.add(target);
            }
        }
        return newTargets;
    }
}
