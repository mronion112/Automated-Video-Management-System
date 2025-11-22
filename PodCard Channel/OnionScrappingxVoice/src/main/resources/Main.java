import org.example.Selenium;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


import java.util.List;

import static org.example.VbeeSelenium.*;

public static String DataMenu(String url, List<String> Data, String NameFileTxt){

    Document doc = null;
    try {
        doc = Jsoup.connect(url)
                .userAgent("Onion216").get();
        String title = Objects.requireNonNull(doc.select("h3.story-name")).text();
        NameFileTxt = title;
        Data.add(title);
        Elements Introduce = doc.select("div.story-detail__top--desc p");
        for(Element element : Introduce) {
            Data.add(element.text());
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
    System.out.println("Thêm menu truyện thành công");
    dataDetail(Selenium.UrlChapter1(url), Data, 0);

    return NameFileTxt;

}


public static List<String> dataDetail(String url,List<String> DataChapter, int count ){

    Document doc = null;
    count++;
    try{
        doc = Jsoup
                .connect(url)
                .userAgent("Onion216")
                .timeout(600000).get();

        Elements list = doc.select("div.chapter-content.mb-3 p");



        for(Element e : list){
            DataChapter.add(e.text());
        }

        System.out.println("Done chapter " + count);


    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    String next_url = doc.select("a.btn.btn-success.chapter-next").attr("href");
    if(next_url == null || next_url.isEmpty() || next_url.isBlank()){
        System.out.println("Finished all " + count + " chapter ");

        return DataChapter;
    }
    else{
        return dataDetail(next_url, DataChapter, count);
    }



}

public static void writeFile(String Storage, List<String> DataChapter, String NameFileTxt){

    String FILEPATH = Storage + "\\" +NameFileTxt +".txt";
    File file = new File(FILEPATH);
    try{
        if(file.createNewFile()){
            System.out.println("Tạo file "+ NameFileTxt + ".txt thành công" );
        }
        else {
            System.out.println("File này đã tồn tại");
        }
    } catch (IOException e) {
        System.out.println("Lỗi tạo file");
        throw new RuntimeException(e);
    }


    try(BufferedWriter writer = new BufferedWriter(new FileWriter(FILEPATH,  false))) {



        for(String s : DataChapter){
            if(s.length() == 1 || s.length() == 2 || s.length() == 3){
                continue;

            }

            else{
                s = s.replaceAll("ch/{1,2}ết","chết");
                s = s.replaceAll("t/{1,2}ự t/{1,2}ử", "tự tử");
                s = s.replace("[ Hoàn ]", "");
                s = s.replace("[ HẾT ]", "");
                writer.write(s);
                writer.newLine();
            }

        }
        System.out.println("Writing file successfully");
    }catch(IOException e){
        System.out.println("Không tìm đc file");
        e.printStackTrace();
    }
}





public static void main(String[] args) throws IOException, InterruptedException {
    Scanner kb = new Scanner(System.in);

    System.out.println("""
            ███╗   ███╗██████╗      ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗\s
                    ████╗ ████║██╔══██╗    ██╔═══██╗████╗  ██║██║████╗  ██║██╔════╝\s
                    ██╔████╔██║██████╔╝    ██║   ██║██╔██╗ ██║██║██╔██╗ ██║██║  ███╗
                    ██║╚██╔╝██║██╔══██╗    ██║   ██║██║╚██╗██║██║██║╚██╗██║██║   ██║
                    ██║ ╚═╝ ██║██║  ██║    ╚██████╔╝██║ ╚████║██║██║ ╚████║╚██████╔╝
                    ╚═╝     ╚═╝╚═╝  ╚═╝     ╚═════╝ ╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝\s
                                               Mr.Onion
        Welcome to the OnionTool convert link story --> voice using https://vbee.vn/
        + We support for only https://tieuhoadan.net/ story website
        + You MUST have the Vbee account which can sign in with user and password
        
        However enjoy my tool ヾ(≧▽≦*)o
        
                    """);

    System.out.print("Nơi lưu file.txt script : ");
    String Storage = kb.nextLine();

    System.out.print("Link website menu : ");
    String url_menu  = kb.nextLine();


    String NameFileTxt = "";

    List<String> Data = new ArrayList<>();
    NameFileTxt = DataMenu(url_menu, Data, NameFileTxt);
    System.out.println("Tên file : " +NameFileTxt);

    writeFile(Storage, Data, NameFileTxt);


    String FILE_PATH = Storage + "\\" +NameFileTxt +".txt";
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
