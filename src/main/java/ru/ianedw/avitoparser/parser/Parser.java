package ru.ianedw.avitoparser.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.ianedw.avitoparser.util.CustomLinkedHashMap;
import ru.ianedw.avitoparser.models.Update;
import ru.ianedw.avitoparser.models.Post;
import ru.ianedw.avitoparser.models.Target;
import ru.ianedw.avitoparser.services.TargetsService;

import java.io.IOException;
import java.util.*;

@Component
public class Parser {
    Logger log = LoggerFactory.getLogger(Parser.class);
    private final Map<Integer, CustomLinkedHashMap<String, Post>> availablePosts;
    private final TargetsService targetService;
    private Set<Target> targets;

    @Autowired
    public Parser(TargetsService targetService) {
        this.targetService = targetService;
        availablePosts = new HashMap<>();
        loadAvailablePosts();
    }

    public Update getUpdate() {
        Update update = new Update();
        update.setTargetPosts(availablePosts);
        return update;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 20000)
    private void updateAvailablePosts() {
        updateTargets();
        for (Target target : targets) {
            parsePosts(target);
        }
    }

    private void loadAvailablePosts() {
        updateTargets();
        for (Target target : targets) {
            loadAvailablePostsByTarget(target);
        }
    }

    private void loadAvailablePostsByTarget(Target target) {
        CustomLinkedHashMap<String, Post> availablePosts = new CustomLinkedHashMap<>(75);
        Elements itemCatalog = getItemsCatalogFromTarget(target);
        for (Element element : itemCatalog) {
            Post post = parsePost(element, -1);
            if (post != null) {
                availablePosts.putItFirst(post.getLink(), post);
            }
        }
        this.availablePosts.put(target.getId(), availablePosts);
    }

    private void parsePosts(Target target) {
        int targetId = target.getId();
        CustomLinkedHashMap<String, Post> availablePosts = this.availablePosts.get(targetId);

        Elements itemsCatalog = getItemsCatalogFromTarget(target);

        for (Element element : itemsCatalog) {
            Post post = parsePost(element, targetId);
            if (post == null) {
                continue;
            }
            availablePosts.put(post.getLink(), post);
        }

        this.availablePosts.put(targetId, availablePosts);
        log.info("Размер availablePosts для цели " + targetId + ": " + availablePosts.size());
    }

    private Elements getItemsCatalogFromTarget(Target target) {
        Document document = null;
        try {
            document = Jsoup.connect(target.getLink()).get();
        } catch (IOException e) {
            log.warn("Ошибка соединения JSOUP");
        }

        Element d = null;
        try {
            d = document.getElementsByAttributeValue("data-marker", "catalog-serp").stream().findAny().orElseThrow();
        } catch (NoSuchElementException | NullPointerException e) {
            log.warn("Ошибка парсинга каталога");
        }

        Elements itemsCatalog = null;
        try {
            itemsCatalog = d.getElementsByAttributeValue("data-marker", "item");
            log.info("Размер считанного каталога: " + itemsCatalog.size());
        } catch (NullPointerException e) {
            log.warn("Ошибка парсинга объявлений из каталога");
        }
        return itemsCatalog;
    }

    private Post parsePost(Element element, int targetId) {

        String link = null;
        try {
            Element elLink = element.getElementsByTag("a").first();
            link = "https://www.avito.ru" + Objects.requireNonNull(elLink).attr("href");
        } catch (NullPointerException e) {
            log.warn("Ошибка парсинга ссылки");
        }

        if (targetId > 0) {
            if (isLinkContained(targetId, link)) {
                return null;
            }
        }

        Post post = new Post();
        String name = null;
        try {
            name = element.getElementsByTag("h3").html();
        } catch (NullPointerException e) {
            log.warn("Ошибка парсинга имени");
        }

        Element elPrice = null;
        try {
            elPrice = element.getElementsByAttributeValue("itemprop", "price").first();
        } catch (NullPointerException e) {
            log.warn("Ошибка парсинга цены");
        }

        int price = 0;
        try {
            price = Integer.parseInt(Objects.requireNonNull(elPrice).attr("content"));
        } catch (NumberFormatException e) {
            log.warn("Ошибка парсинга цены Integer.parseInt()");
        }

        post.setLink(link);
        post.setPrice(price);
        post.setName(name);

        return post;
    }

    private boolean isLinkContained(int targetId, String link) {
        return availablePosts.get(targetId).containsKey(link);
    }
    private void updateTargets() {
        List<Target> updatedList = targetService.findAll();
        if (targets == null) {
            targets = new HashSet<>(updatedList);
            return;
        }

        if (updatedList.size() != targets.size()) {
            for (Target target : updatedList) {
                if (targets.add(target)) {
                    loadAvailablePostsByTarget(target);
                }
            }
        }
    }
}
