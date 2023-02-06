package ru.ianedw.avitoparser.config;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {
    private final Environment environment;

    @Autowired
    public Config(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public WebDriver webDriver() {
        System.setProperty("webdriver.chrome.driver", Objects.requireNonNull(environment.getProperty("selenium.chrome.driver")));
        WebDriver driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1050, 750));
        return driver;
    }

    @Bean
    public Actions actions() {
        return new Actions(webDriver());
    }
}
