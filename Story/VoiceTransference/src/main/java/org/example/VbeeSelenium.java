package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VbeeSelenium {

    public static void killChromeWithProfile(String profilePath) {
        try {
            // escape ký tự \ cho đúng format Windows command
            String escapedPath = profilePath.replace("\\", "\\\\");
            String command = "wmic process where \"CommandLine like '%" + escapedPath + "%' and Name='chrome.exe'\" call terminate";
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println("🧹 Đã đóng Chrome dùng profile: " + profilePath);
        } catch (Exception e) {
            System.out.println("⚠ Không thể đóng Chrome của profile này: " + e.getMessage());
        }
    }

    public static WebDriver LoginVbee(String username, String password) {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        // Trước khi mở Chrome mới, dọn process cũ

        String profilePath = "C:\\Users\\Mr.Onion216\\Desktop\\vbee_profile";
        killChromeWithProfile(profilePath);

        File profileDir = new File(profilePath);
        if (!profileDir.exists()) {
            profileDir.mkdirs();
            System.out.println("🆕 Đã tạo thư mục profile mới tại: " + profilePath);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + profilePath);
        options.addArguments("profile-directory=Default");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");

        // ❌ KHÔNG dùng headless với user profile
        // Nếu muốn headless, dùng tạm chế độ fake UI:
        // options.addArguments("--headless=new");  // chỉ bật nếu thực sự cần

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            driver.get("https://vbee.vn/");
            Thread.sleep(100);
            if (driver.getCurrentUrl().contains("https://studio.vbee.vn")) {
                System.out.println("🎉 Đã đăng nhập sẵn, bỏ qua bước login!");
                return driver;
            }

            WebElement loginBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login-button")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));

            driver.findElement(By.id("username")).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys(password);
            By loginConfirmXPath = By.xpath("//button[.//p[contains(normalize-space(.),'Đăng nhập')]]");
            WebElement loginConfirmBtn = wait.until(ExpectedConditions.elementToBeClickable(loginConfirmXPath));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", loginConfirmBtn);
            Thread.sleep(100);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginConfirmBtn);
            System.out.println("✅ Đã click vào nút Đăng nhập");

            wait.until(ExpectedConditions.urlContains("https://studio.vbee.vn/studio/text-to-speech"));
            System.out.println("🎉 Đăng nhập thành công! URL hiện tại: " + driver.getCurrentUrl());


        } catch (Exception e) {
            System.out.println("Sai tài khoản hoặc mật khẩu ");
        }
        return driver;


    }

    public static String DataScript(String FILE_PATH) throws IOException {
        StringBuilder Data = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))){
            String line;
            while((line = br.readLine()) != null){
                Data.append(line);
            }
        }

        return Data.toString();

    }

    public static String DataFileName(String FILE_PATH) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))){
            return br.readLine();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public static void VideoProcess(String DataScript, WebDriver driver, String NameData) throws IOException, InterruptedException {
        closeAllPopups(driver);
        System.out.println("Running video process");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            WebDriverWait waitPop = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement cancelBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(text(),'Hủy')]")  // hoặc dùng CSS selector tương ứng
                    )
            );
            cancelBtn.click();
            System.out.println("Đã tự động bấm Hủy");
        } catch (TimeoutException e) {
            System.out.println("Không thấy popup hỏi tải lại");
        }


        By settingSpeedPlace = By.cssSelector("input[id=\"mui-6\"]");
        WebElement settingSpeed = wait.until(ExpectedConditions.visibilityOfElementLocated(settingSpeedPlace));
        settingSpeed.click();
        settingSpeed.sendKeys(Keys.CONTROL + "a");
        settingSpeed.sendKeys("1.1");


        System.out.println(NameData);
        By settingNameVideo = By.cssSelector("input[class=\"size-input\"]");
        WebElement settingNameVideoElement = wait.until(ExpectedConditions.visibilityOfElementLocated(settingNameVideo));
        settingNameVideoElement.sendKeys(Keys.CONTROL + "a");
        settingNameVideoElement.sendKeys(NameData);

        WebDriverWait waitProgress = new WebDriverWait(driver, Duration.ofSeconds(36000));


        WebElement textBox = waitProgress.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("div[role='textbox'][contenteditable='true']")
                )
        );
        textBox.sendKeys(DataScript);
        System.out.println("Add Script done");
        wait.until(ExpectedConditions.textToBePresentInElement(textBox, DataScript));
        System.out.println("✅ Text fully added into textbox!");


        By convertButton = By.cssSelector("button[id = 'convert-tts']");
        WebElement convertBtn = wait.until(ExpectedConditions.elementToBeClickable(convertButton));
        convertBtn.click();
        System.out.println("Convertdone");
        Thread.sleep(2000);

        By finish = By.xpath("//p[contains(text(),'Nghe audio')]");
        WebElement finishBtn = waitProgress.until(ExpectedConditions.presenceOfElementLocated(finish));
        System.out.println("Đã load xong video ");
        Thread.sleep(1000);
        By downloadButton = By.cssSelector("div[data-id='download-curr-audio']");
        WebElement downloadBtn = waitProgress.until(ExpectedConditions.elementToBeClickable(downloadButton));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", downloadBtn);
        System.out.println("⬇️ Đã bấm nút tải xuống");




    }
    public static void closeAllPopups(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // Alert (JS)
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
            System.out.println("🔕 Đã đóng alert JavaScript!");
        } catch (TimeoutException ignored) {}

        try {
            // HTML popup có nút Hủy / Đóng
            List<WebElement> buttons = driver.findElements(
                    By.xpath("//button[contains(text(),'Đóng') or contains(text(),'Hủy') or contains(text(),'×')]")
            );
            for (WebElement b : buttons) {
                if (b.isDisplayed()) {
                    b.click();
                    System.out.println("🔕 Đã bấm nút đóng popup");
                }
            }
        } catch (Exception ignored) {}

        try {
            // Xóa popup kiểu overlay
            js.executeScript(
                    "document.querySelectorAll('.MuiDialog-root, .MuiSnackbar-root, .popup, .modal').forEach(el => el.remove());"
            );
            System.out.println("🧹 Đã xóa popup quảng cáo khỏi DOM");
        } catch (Exception ignored) {}
    }






    public static void main(String[] args) throws IOException, InterruptedException {
//        String username = "mr.onion112@gmail.com";
//        String password = "@Queanhhy0907";

        String FILE_PATH = "C:\\Users\\Mr.Onion216\\Desktop\\DataScrap.txt";
        Scanner kb = new Scanner(System.in);
        String NameData = "NameData is null";
            System.out.print("Nhập vào tài khoản : ");
            String username = kb.nextLine();
            System.out.print("Nhập vào mật khẩu : ");
            String password = kb.nextLine();


        WebDriver driver =  LoginVbee(username, password);

        String DataScript = DataScript(FILE_PATH);
        System.out.println(DataScript);

        VideoProcess(DataScript, driver, DataFileName(FILE_PATH));



    }
}
