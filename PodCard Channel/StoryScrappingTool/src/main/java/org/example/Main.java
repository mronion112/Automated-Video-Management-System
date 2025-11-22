package org.example;

import org.example.TieuHoaDanController.DataStoryFromUrl;
import org.example.TieuHoaDanController.FolderManagement;
import org.example.TieuHoaDanModun.MenuStory;
import org.example.TieuHoaDanModun.StoryChapter;
import org.example.TieuHoaDanModun.WebStory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.*;


public class Main {

    public static List<String> getNewUrls(List<String> listUrls, String folderDir) throws IOException {
        System.out.println("Bắt đầu quét thông tin web");
        List<String> newUrls = new ArrayList<>();
        int counter = 1;
        for(String url : listUrls) {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            Elements titleEl = doc.select("h3.story-name");

            Path parentDir = Paths.get(folderDir);
            Path folderChecker = parentDir.resolve(titleEl.text().trim());

            if (Files.exists(folderChecker)) {
                continue;

            }
            else{
                newUrls.add(url);
            }
            System.out.println("Đang quét "+ counter+"/"+listUrls.size());
            counter++;
        }
        return  newUrls;

    }


    public static void main(String[] args) throws IOException {

//        ChromeOptions options = new ChromeOptions();
//        options.setBinary("/usr/bin/brave-browser"); // Đảm bảo đường dẫn đúng
//        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
//
//        WebDriver driver = new ChromeDriver(options);
//        driver.get("https://www.tieuhoadan.net/truyen/nu-phu-ac-doc-bat-bai");
//        String html = driver.getPageSource();
//        driver.quit();
//
//        Document doc = Jsoup.parse(html);
//        MenuStory story = new MenuStory("https://www.tieuhoadan.net/truyen/thanh-dieu");
//        story.fetchData();
//        System.out.printf(story.toString());

//        DataStoryFromUrl dataStoryFromUrl = new DataStoryFromUrl("https://www.tieuhoadan.net/truyen/chi-nguyen-vi-nang");
//        dataStoryFromUrl.setup();
//
//
//        FolderManagement folderManagement = new FolderManagement("/home/mronion216/Documents/Onion Code/OnionTool/StoryScrappingTool/OutPut",
//                                                                    dataStoryFromUrl.getTitleStory(),
//                                                                    dataStoryFromUrl.getDescriptionStory(),
//                                                                    dataStoryFromUrl.getdataStory());
//        folderManagement.finnalMethod();


        System.out.println("""
            ███╗   ███╗██████╗      ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗\s
                    ████╗ ████║██╔══██╗    ██╔═══██╗████╗  ██║██║████╗  ██║██╔════╝\s
                    ██╔████╔██║██████╔╝    ██║   ██║██╔██╗ ██║██║██╔██╗ ██║██║  ███╗
                    ██║╚██╔╝██║██╔══██╗    ██║   ██║██║╚██╗██║██║██║╚██╗██║██║   ██║
                    ██║ ╚═╝ ██║██║  ██║    ╚██████╔╝██║ ╚████║██║██║ ╚████║╚██████╔╝
                    ╚═╝     ╚═╝╚═╝  ╚═╝     ╚═════╝ ╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝\s
                                               Mr.Onion
        Welcome to the OnionTool convert link story to text
        + We support for only https://tieuhoadan.net/ story website
        NOTE : You must choose a type in tieuhoadan websit and push the link into   
       
        Enjoy my tool ヾ(≧▽≦*)o
        
                    """);
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Encoding hiện tại: " + System.getProperty("file.encoding"));
        Scanner scanner = new Scanner(System.in);

        System.out.print("Your Folder (enter to use default folder): ");
        String folderDir = scanner.nextLine();

        if(folderDir.equals("")){
            folderDir = "OutPut" + File.separator;
        }

        WebStory webStory = new WebStory("https://www.tieuhoadan.net/the-loai/hien-dai#");
        webStory.collectAllUrlsStory(folderDir);

        int count = 1;
        int end = webStory.getListUrlsStoryWeb().size();

        int add = 0;

        for(String url :  webStory.getListUrlsStoryWeb()) {

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            Elements titleEl = doc.select("h3.story-name");

            Path parentDir = Paths.get(folderDir);
            Path folderChecker = parentDir.resolve(titleEl.text().trim().toUpperCase());

            if (Files.exists(folderChecker)) {
                System.out.println("\n" + count + "/" + end + " skip....  ");

            }


            else {
                System.out.println("\n" + count + "/" + end + " đã thêm mới  ");
                DataStoryFromUrl dataStoryFromUrl = new DataStoryFromUrl(url);
                try {
                    dataStoryFromUrl.setup();
                } catch (IllegalFormatWidthException e) {
                    System.out.println("Lỗi format url Menu truyện");
                } catch (FormatFlagsConversionMismatchException e) {
                    System.out.println("Lỗi cờ url Menu truyện ");
                } catch (Exception e) {
                    System.out.println("Có gì đó lỗi url Menu truyện ....");
                }

                FolderManagement folderManagement = new FolderManagement(folderDir,
                        dataStoryFromUrl.getTitleStory(),
                        dataStoryFromUrl.getDescriptionStory(),
                        dataStoryFromUrl.getdataStory());

                if (folderManagement.finnalMethod()) {
                    System.out.println("Đã xong hết quá trình !!!\n");
                    add++;

                } else {
                    System.out.println("Truyện đã tồn tại skip....\n");
                }
            }
            count++;

        }

        System.out.println("Đã thêm "+add+" truyện mới ");


    }
}
