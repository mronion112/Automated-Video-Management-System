package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


import java.util.List;


public class Selenium {
    public static String UrlChapter1(String urlMenu){
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");


        WebDriver driver = new ChromeDriver(options);


        driver.get(urlMenu);


        List<WebElement> chapterLinks = driver.findElements(By.cssSelector(
                "div.story-detail__list-chapter--list__item ul li a"
        ));

        if (!chapterLinks.isEmpty()) {
            WebElement firstLink = chapterLinks.get(0);
            String href = firstLink.getAttribute("href");
            String title = firstLink.getText();

            System.out.println("Chương đầu tiên: " + title);
            System.out.println("Link: " + href);
            return href;
        } else {
            System.out.println("Không tìm thấy chương nào!");
        }
        driver.quit();
        return null;
    }



}

