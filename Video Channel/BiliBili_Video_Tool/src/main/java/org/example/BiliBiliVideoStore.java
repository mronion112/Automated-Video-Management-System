package org.example;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BiliBiliVideoStore {

    public static void killChromeWithProfile(String profilePath) {
        try {
            String escapedPath = profilePath.replace("\\", "\\\\");
            String command = "wmic process where \"CommandLine like '%" + escapedPath + "%' and Name='chrome.exe'\" call terminate";
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println("Đã đóng Chrome dùng profile: " + profilePath);
        } catch (Exception e) {
            System.out.println("Không thể đóng Chrome của profile này: " + e.getMessage());
        }
    }

    public static ArrayList<Video> listUrlVideos(String urlChannel) throws InterruptedException {
        ArrayList<Video> VideoData = new ArrayList<>();
        WebDriverManager.chromedriver()
                .cachePath("driver_cache")   // thư mục lưu cache
                .setup();

        String currentDir = System.getProperty("user.dir");
//            System.setProperty("webdriver.chrome.driver", currentDir + File.separator + "chromedriver.exe");

        String profilePath = currentDir + File.separator + "Onion_profile";
        killChromeWithProfile(profilePath);

        File profileDir = new File(profilePath);
        if (!profileDir.exists() && profileDir.mkdirs()) {
            System.out.println(" Đã tạo thư mục profile mới tại: " + profilePath);
        }

        ChromeOptions options = new ChromeOptions();
//            options.addArguments("--headless");
        options.addArguments("--log-level=3"); // ERROR only
        options.addArguments("--silent");
        options.addArguments("--disable-logging");
        options.addArguments("user-data-dir=" + profilePath);
        options.addArguments("profile-directory=Default");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");

        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        Logger.getLogger("org.openqa.selenium.remote").setLevel(Level.OFF);
        Logger.getLogger("org.openqa.selenium.chromium").setLevel(Level.OFF);
        Logger.getLogger("io.netty").setLevel(Level.OFF);


        ChromeDriverService service = new ChromeDriverService.Builder()
                .withSilent(true)
                .withLogOutput(new OutputStream() { @Override public void write(int b) {} })
                .build();

//        WebDriver driver = new ChromeDriver(service, options);


        ChromeDriver driver = null;

        try {
            driver = new ChromeDriver(service, options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            driver.get(urlChannel);

            Thread.sleep(3000);

            boolean loaded = false;
            Thread.sleep(1000);
                try {
                    WebElement firstVideo = null;
                    WebElement close_button = null;
                    int maxRefresh = 10;
                    int refreshCount = 0;

                    while (refreshCount < maxRefresh) {
                        try {
                            firstVideo = new WebDriverWait(driver, Duration.ofSeconds(1))
                                    .until(ExpectedConditions.visibilityOfElementLocated(
                                            By.cssSelector("div.bili-video-card__title a")
                                    ));
                            break; // Tìm thấy phần tử → thoát vòng lặp
                        } catch (TimeoutException e) {
                            refreshCount++;
                            if (refreshCount < maxRefresh) {
                                close_button = new WebDriverWait(driver, Duration.ofSeconds(2))
                                        .until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.bili-mini-close-icon")));
                                close_button.click();
                                Thread.sleep(1000);
                                driver.navigate().refresh();
                            }
                        }
                    }
                    if(refreshCount == maxRefresh){
                        System.out.println("Can't access bili video channel ");
                        return new ArrayList<>();
                    }

                    if (firstVideo == null) {
                        System.out.println("Không tìm thấy phần tử sau " + maxRefresh + " lần refresh.");
                    }





                    boolean hasNext = true;
                    int PageScan = 0;

                    do {
                        // 🔹 Lấy danh sách video hiện tại
                        List<WebElement> videoElements = driver.findElements(By.cssSelector("div.bili-video-card__title a"));
                        for (WebElement e : videoElements) {
                            String title = e.getText();
                            String href = e.getAttribute("href");
                            if (href != null && !href.isBlank()) {
                                VideoData.add(new Video(title, href));
                            }
                        }
                        PageScan++;

                        // 🔹 Cuộn xuống cuối trang
                        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                        Thread.sleep(1000);

                        try {
                            // 🔹 Tìm nút có text = "下一页"
                            WebElement nextButton = driver.findElement(By.xpath("//button[contains(text(),'下一页')]"));

                            // 🔹 Kiểm tra xem nút còn hoạt động không
                            if (nextButton.isEnabled() && nextButton.getAttribute("disabled") == null) {
                                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
                                Thread.sleep(500);
                                nextButton.click();
                                System.out.println("Chuyển sang trang tiếp theo...");
                                Thread.sleep(2000);
                            } else {
                                System.out.println("Hết trang, dừng lại.");
                                hasNext = false;
                            }

                        } catch (NoSuchElementException e) {
                            System.out.println("❌ Không tìm thấy nút 下一页 — dừng lại.");
                            hasNext = false;
                        }

                    } while (hasNext);
                    System.out.println("Số trang đã quét : " + PageScan);
                    System.out.println("Tổng số video quét được: " + VideoData.size());


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (driver != null) {
                        driver.quit();
                    }
                }


                return VideoData;



        } catch (Exception e) {
            e.printStackTrace();
        }
        return VideoData;
    }
}