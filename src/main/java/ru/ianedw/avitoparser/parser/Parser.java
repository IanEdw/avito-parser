package ru.ianedw.avitoparser.parser;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import ru.ianedw.avitoparser.util.Months;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class Parser {
    Logger log = LoggerFactory.getLogger(Parser.class);
    private final WebDriver browser;
    private final JavascriptExecutor js;
    private final Actions actions;
    private final TargetsService targetService;
    private final Map<Integer, TreeSet<Post>> availablePosts;
    private Map<Target, String> targetsWithTheirWindows;
    private int yPointInBrowser;



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

    @Scheduled(initialDelay = 15000,fixedDelay = 15000)
    private void updateAvailablePosts() {
        List<Target> newTargets = updateTargets();
        loadPosts(newTargets);

        targetsWithTheirWindows.keySet().forEach(this::updatePosts);
    }

    private void updatePosts(Target target) {
        browser.switchTo().window(targetsWithTheirWindows.get(target));
        WebElement findButton = browser.findElement(
                By.xpath("//button[@data-marker='search-form/submit-button']")
        );
        actions.click(findButton);

        String xpathExpression = "//div[@data-marker='item']";
        List<WebElement> postsOnPage = browser.findElements(By.xpath(xpathExpression));
        int size = postsOnPage.size();

        for (int i = 1; i < size + 1; i++) {
            updatePost(xpathExpression, i, target.getId());
        }
    }
    private void updatePost(String parentBlockXPathExpression, int blockNumber, int targetId) {
        String parentExpression = String.format(parentBlockXPathExpression + "[%d]", blockNumber);
        Post post = new Post();
        post.setLink(findPostLink(parentExpression));

        if (isContainedInAvailablePosts(targetId, post)) {
            return;
        }

        TreeSet<Post> targetPosts = availablePosts.get(targetId);

        post.setName(findPostName(parentExpression));
        post.setPrice(findPostPrice(parentExpression));
        post.setPublicationTime(findPostPublicationTime(parentExpression));

        if (targetPosts.size() > 75) {
            for (int i = 0; i < 10; i++) {
                targetPosts.pollFirst();
            }
        }
        availablePosts.get(targetId).add(post);
    }


    private void loadPosts(List<Target> targets) {
        targets.forEach(this::loadPosts);
    }
    private void loadPosts(Target target) {
        TreeSet<Post> posts = new TreeSet<>((o1, o2) -> {
            LocalDateTime publicationTimeO1 = o1.getPublicationTime();
            LocalDateTime publicationTimeO2 = o2.getPublicationTime();
            if (publicationTimeO1.isBefore(publicationTimeO2)) {
                return -1;
            } else if (publicationTimeO1.isAfter(publicationTimeO2)) {
                return 1;
            } else {
                return 0;
            }
        });

        if (targetsWithTheirWindows.get(target) == null) {
            targetsWithTheirWindows.put(target, openNewWindow());
        }
        browser.switchTo().window(targetsWithTheirWindows.get(target));
        browser.get(target.getLink());

        String xpathExpression = "//div[@data-marker='item']";
        List<WebElement> postsOnPage = browser.findElements(By.xpath(xpathExpression));
        int size = postsOnPage.size();

        for (int i = 1; i < size + 1; i++) {
            posts.add(loadPost(xpathExpression, i));
        }
        js.executeScript("window.scroll(0, 0)");
        yPointInBrowser = 0;
        availablePosts.put(target.getId(), posts);
    }
    private Post loadPost(String parentBlockXPathExpression, int blockNumber) {
        String parentExpression = String.format(parentBlockXPathExpression + "[%d]", blockNumber);

        Post post = new Post();
        post.setName(findPostName(parentExpression));
        post.setLink(findPostLink(parentExpression));
        post.setPrice(findPostPrice(parentExpression));
        post.setPublicationTime(findPostPublicationTime(parentExpression));

        return post;
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

    private LocalDateTime findPostPublicationTime(String parentExpression) {
        WebElement dateBlock = browser.findElement(By.xpath(parentExpression + "//div[@data-marker='item-date']"));
        String date = findDateElement(dateBlock, parentExpression).getText() + " " + Year.now().getValue();

        for (Months value : Months.values()) {
            if (date.contains(value.name())) {
                date = date.replace(value.name(), value.getNumber());
            }
        }
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("d M HH:mm yyyy"));
    }

    private WebElement findDateElement(WebElement dateBlock, String expression) {
        WebElement date;
        while (true) {
            try {
                actions.moveToElement(dateBlock).build().perform();
                Thread.sleep(250);
                date = browser.findElement(By.xpath(expression + "//div[@data-placement='bottom']//span"));
                break;
            } catch (NoSuchElementException e) {
                pageDown();
            } catch (InterruptedException ignored) {
            }
        }
        return date;
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
                newTargets.add(target);
            }
        }
        return newTargets;
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

    private void pageDown() {
        yPointInBrowser += 250;
        String script = String.format("window.scroll(0, %d)", yPointInBrowser);
        js.executeScript(script);
    }

    private boolean isContainedInAvailablePosts(int targetId, Post post) {
        return availablePosts.get(targetId).contains(post);
    }
}
