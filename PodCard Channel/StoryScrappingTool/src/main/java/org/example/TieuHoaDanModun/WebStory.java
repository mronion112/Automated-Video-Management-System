package org.example.TieuHoaDanModun;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//d-block text-decoration-none
//ext-decoration-none text-dark fs-6 hover-title text-one-row story-name
//d-block story-item-full__image
public class WebStory {
    private List<String> listUrlsStoryWeb;
    private String urlWeb;
    public WebStory(String urlWeb) {
        this.listUrlsStoryWeb = null;
        this.urlWeb = urlWeb;
    }

    public boolean collectAllUrlsStory(String folderDir) {
        Document document;
        try {
            document = Jsoup.connect(urlWeb)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            System.out.println("Successfully connected to " + urlWeb);
        }catch (IOException e){
            System.out.println("Can't connect to the website");
            return false;
        }
        if(document == null){
            System.out.printf("Fail to get document websit");
            return false;
        }

        List<String> urls = new ArrayList<>();

        //Hot story urls
        Elements elements1 = document.select("a.d-block.text-decoration-none");
        for(Element element : elements1){

            urls.add(element.attr("href"));
        }



        listUrlsStoryWeb = urls;

        return true;
    }


    public List<String> getListUrlsStoryWeb() {
        return listUrlsStoryWeb;
    }

    public void setListUrlsStoryWeb(List<String> listUrlsStoryWeb) {
        this.listUrlsStoryWeb = listUrlsStoryWeb;
    }

    @Override
    public String toString() {
        return "WebStory{" +
                "\nurlWeb ='" + urlWeb  +
                "\nlistStoryUrls=" + listUrlsStoryWeb.toString() +
                '}';
    }
}
