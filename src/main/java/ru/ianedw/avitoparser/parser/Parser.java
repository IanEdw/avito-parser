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
import ru.ianedw.avitoparser.models.Post;
import ru.ianedw.avitoparser.models.Target;
import ru.ianedw.avitoparser.services.TargetsService;

import java.io.IOException;
import java.util.*;

@Component
public class Parser {
    Logger log = LoggerFactory.getLogger(Parser.class);
    private final Map<Integer, List<Post>> lastPosts;
    private final TargetsService targetService;
    private List<Target> targets;

    @Autowired
    public Parser(TargetsService targetService) {
        this.targetService = targetService;
        lastPosts = new HashMap<>();
    }


    public Map<Integer, List<Post>> getLastPosts(List<Integer> ids) {
        Map<Integer, List<Post>> result = new HashMap<>();
        for (Integer id : ids) {
            if (lastPosts.containsKey(id)) {
                result.put(id, lastPosts.get(id));
            }
        }
        return result;
    }

    @Scheduled(initialDelay = 1000L, fixedDelay = 10000L)
    private void updateLastPosts() {
        updateTargets();
        for (Target target : targets) {
            List<Post> list = parseLastPosts(target.getLink());
            if (list == null) {
                continue;
            }
            lastPosts.put(target.getId(), list);
        }
    }

    private List<Post> parseLastPosts(String link) {
        try {
            Document document = Jsoup.connect(link).get();
            List<Post> result = new ArrayList<>();
            Elements itemsCatalog = getCatalog(document).getElementsByAttributeValue("data-marker", "item");

            for (Element item : itemsCatalog) {
                Post post = getPost(item);
                if (post == null) {
                    continue;
                }
                result.add(getPost(item));
            }

            return result;
        } catch (IOException e) {
            log.warn("Ошибка соединения");
            return null;
        } catch (NullPointerException e) {
            log.warn("getCatalog() вернул null");
            log.warn(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    private Post getPost(Element element) {
        try {
            Post result = new Post();
            String name = element.getElementsByTag("h3").html();

            Element elPrice = element.getElementsByAttributeValue("itemprop", "price").first();
            int price = Integer.parseInt(Objects.requireNonNull(elPrice).attr("content"));

            Element elLink = element.getElementsByTag("a").first();
            String link = "https://www.avito.ru" + Objects.requireNonNull(elLink).attr("href");
            result.setLink(link);
            result.setPrice(price);
            result.setName(name);
            return result;
        } catch (NumberFormatException | NullPointerException e) {
            log.warn("Ошибка парсинга цены или ссылки: " + element.toString());
            log.warn(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    private Element getCatalog(Document document) {
        return document.getElementsByAttributeValue("data-marker", "catalog-serp").stream().findAny().orElse(null);
    }

    private void updateTargets() {
        targets = targetService.findAll();
    }
    //    private void smartUpdate() throws IOException {
//        updateTargets();
//        for (Target target : targets) {
//            Map<String, Post> currentTargetMap;
//            if (lastPosts.containsKey(target.getId())) {
//                currentTargetMap = lastPosts.get(target.getId());
//            } else {
//                currentTargetMap = new HashMap<>();
//            }
//            Element catalog = getCatalog(Jsoup.connect(target.getLink()).get());
//            for (Element item : catalog.getElementsByAttributeValue("data-marker", "item")) {
//                String link = "https://avito.ru" + item.getElementsByTag("a").first().attr("href");
//                if (currentTargetMap.containsKey(link)) {
//                    continue;
//                } else {
//                    if (currentTargetMap.size() > 20) {
//                        currentTargetMap.values().stream().min()
//                    }
//                }
//            }
//        }
//    }
}
