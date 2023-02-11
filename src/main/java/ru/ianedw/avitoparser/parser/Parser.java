package ru.ianedw.avitoparser.parser;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
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

import java.util.*;

@Component
public class Parser {
    Logger log = LoggerFactory.getLogger(Parser.class);
    private final WebDriver browser;
    private final JavascriptExecutor js;
    private final Actions actions;
    private final TargetsService targetService;
    private final Map<Integer, Map<String, Post>> availablePosts;
    private Map<Target, String> targetsWithTheirWindows;
    private int yPointSamePosts;



    @Autowired
    public Parser(WebDriver browser, Actions actions, TargetsService targetService) {
        this.browser = browser;
        this.js = (JavascriptExecutor) browser;
        this.actions = actions;
        this.targetService = targetService;
        availablePosts = new HashMap<>();
        loadPosts(updateTargets());
    }

    public Update getUpdate() {
        Update update = new Update();
        update.setTargetPosts(availablePosts);
        return update;
    }

    @Scheduled(initialDelay = 1000,fixedDelay = 10000)
    private void updateAvailablePosts() {
        log.info("Запущено обновление объявлений");
        List<Target> newTargets = updateTargets();
        loadPosts(newTargets);

        for (Target target : targetsWithTheirWindows.keySet()) {
            log.info("Обновление цели " + target.getId());
            updatePosts(target);
        }
    }

    private void updatePosts(Target target) {
        String xpathExpression = "//div[@data-marker='catalog-serp']//div[@data-marker='item']";
        browser.switchTo().window(targetsWithTheirWindows.get(target));
        WebElement findButton;
        try {
            findButton = browser.findElement(By.xpath("//button[@data-marker='search-form/submit-button']"));
            actions.click(findButton).build().perform();
        } catch (NoSuchElementException | TimeoutException e) {
            browser.get(target.getLink());
        }
        List<WebElement> postsOnPage = browser.findElements(By.xpath(xpathExpression));
        log.info("На странице " + postsOnPage.size() + " объявлений");
        yPointSamePosts = getYPointSamePosts();

        for (int i = 1; i < postsOnPage.size() + 1; i++) {
            updatePost(xpathExpression, i, target.getId());
        }
        log.info("Размер availablePosts для Target " + target.getId() + " = " + availablePosts.get(target.getId()).size());
    }

    private void updatePost(String parentBlockXPathExpression, int blockNumber, int targetId) {
        String expression = String.format(parentBlockXPathExpression + "[%d]", blockNumber);
        String link = findPostLink(expression);
        int blockYPoint = browser.findElement(By.xpath(expression)).getLocation().y;
        Map<String, Post> targetPosts = availablePosts.get(targetId);

        if (blockYPoint > yPointSamePosts) {
            return;
        } else if (!isPostPublicationTimeValid(expression)) {
            targetPosts.remove(link);
            return;
        }

        Post post = new Post();

        post.setLink(link);
        post.setName(findPostName(expression));
        post.setPrice(findPostPrice(expression));

        targetPosts.put(link, post);
    }


    private void loadPosts(List<Target> targets) {
        targets.forEach(this::loadPosts);
    }

    private void loadPosts(Target target) {
        Map<String, Post> posts = new HashMap<>();

        if (targetsWithTheirWindows.get(target) == null) {
            targetsWithTheirWindows.put(target, openNewWindow());
        }
        browser.switchTo().window(targetsWithTheirWindows.get(target));
        browser.get(target.getLink());

        String xpathExpression = "//div[@data-marker='catalog-serp']//div[@data-marker='item']";
        List<WebElement> postsOnPage = browser.findElements(By.xpath(xpathExpression));
        yPointSamePosts = getYPointSamePosts();

        for (int i = 1; i < postsOnPage.size() + 1; i++) {
            try {
                Post post = loadPost(xpathExpression, i);
                posts.put(post.getLink(), post);
            } catch (NotTargetPost ignored) {
                break;
            }
        }

        availablePosts.put(target.getId(), posts);
        log.info("Загружено " + posts.size() + " объявлений");
    }
    private Post loadPost(String parentBlockXPathExpression, int blockNumber) {
        String expression = String.format(parentBlockXPathExpression + "[%d]", blockNumber);
        int blockYPoint = browser.findElement(By.xpath(expression)).getLocation().y;

        if (blockYPoint > yPointSamePosts) {
            throw new NotTargetPost();
        }

        if (isPostPublicationTimeValid(expression)) {
            Post post = new Post();

            post.setName(findPostName(expression));
            post.setLink(findPostLink(expression));
            post.setPrice(findPostPrice(expression));

            return post;
        } else {
            throw new NotTargetPost();
        }
    }


    private boolean isPostPublicationTimeValid(String parentExpression) {
        WebElement dateBlock = browser.findElement(By.xpath(parentExpression + "//div[@data-marker='item-date']"));
        String[] dateArray = dateBlock.getText().split(" ");
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

    private String findPostName(String parentExpression) {
        return browser.findElement(By.xpath( parentExpression + "//a[@data-marker='item-title']")).getAttribute("title");
    }

    private String findPostLink(String parentExpression) {
        return browser.findElement(By.xpath( parentExpression + "//a[@data-marker='item-title']")).getAttribute("href");
    }

    private int findPostPrice(String parentExpression) {
        return Integer.parseInt(browser.findElement(By.xpath(parentExpression + "//meta[@itemprop='price']")).getAttribute("content"));
    }



    private List<Target> updateTargets() {
        List<Target> updatedList = targetService.getAll();
        if (targetsWithTheirWindows == null) {
            targetsWithTheirWindows = new HashMap<>();
            updatedList.forEach(target -> targetsWithTheirWindows.put(target, null));
            return updatedList;
        }

        List<Target> newTargets = new ArrayList<>();
        for (Target target : updatedList) {
            if (!targetsWithTheirWindows.containsKey(target)) {
                targetsWithTheirWindows.put(target, null);
                newTargets.add(target);
            }
        }
        return newTargets;
    }

    private int getYPointSamePosts() {
        int result = Integer.MAX_VALUE;
        WebElement samePosts = null;
        WebElement samePostsInOtherCities = null;
        try {
            samePosts = browser.findElement(By.xpath("//div[text()='Похоже на то, что вы ищете']"));
        } catch (NoSuchElementException ignored) {
        }
        try {
            samePostsInOtherCities = browser.findElement(By.xpath("/div[contains(text(),'других городах')]"));
        } catch (Exception ignored) {
        }
        if (samePosts != null) {
            int samePostY = samePosts.getLocation().y;
            if (samePostY < result) {
                result = samePostY;
            }
        }
        if (samePostsInOtherCities != null) {
            int samePostsIntOtherCitiesY = samePostsInOtherCities.getLocation().y;
            if (samePostsIntOtherCitiesY < result) {
                result = samePostsIntOtherCitiesY;
            }
        }
        return result;
    }

    private String openNewWindow() {
        Set<String> openedWindows = browser.getWindowHandles();
        js.executeScript("window.open()");
        Set<String> newOpenedWindows = browser.getWindowHandles();

        String newWindow = null;
        for (String window : newOpenedWindows) {
            if (!openedWindows.contains(window)) {
                newWindow = window;
            }
        }
        return newWindow;
    }
}
