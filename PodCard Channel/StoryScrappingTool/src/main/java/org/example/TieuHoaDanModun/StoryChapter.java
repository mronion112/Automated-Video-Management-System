package org.example.TieuHoaDanModun;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoryChapter {
//    replaceAll("[/\\\\~#*]", "")

    private String urlChapter;
    private Document document;
    private List<String> dataChapter;
//    private String urlNextChapter;

    public StoryChapter(String urlChapter) {
        this.urlChapter = urlChapter;
        this.document = null;
        this.dataChapter = null;
//        this.urlNextChapter = "";
    }

    public void scrappingDataChapter() {
        try {
            document = Jsoup.connect(this.urlChapter)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
        }catch(IOException e){
            System.out.println("Can't take the document of "+this.urlChapter);
        }

        Elements elements = document.select("div.chapter-content.mb-3 p");

        List<String> data = new ArrayList<>();
        for(Element element : elements){
            String line = element.text().trim().replaceAll("[/\\\\~#*]", "").
                                                replace("c h ế t","chết").
                                                replace("c h ó", "chó");
            if(line.length()<=3) {
                continue;
            }
            data.add(line);
        }

        dataChapter = data;


    }

    public String getUrlChapter() {
        return urlChapter;
    }

    public void setUrlChapter(String urlChapter) {
        this.urlChapter = urlChapter;
    }

    public Document getDocument() {
        return document;
    }
    public void setDocument(Document document) {
        this.document = document;
    }

    public List<String> getDataChapter() {
        return dataChapter;
    }

    public void setDataChapter(List<String> dataChapter) {
        this.dataChapter = dataChapter;
    }
//    public String getUrlNextChapter() {
//        urlNextChapter = document.select("a.btn.btn-success.chapter-next").attr("href");
//        return urlNextChapter;
//    }
//    public void setUrlNextChapter(String urlNextChapter) {
//        this.urlNextChapter = urlNextChapter;
//    }


    @Override
    public String toString() {
        return "StoryChapter{" +
                "\nurlChapter=" + urlChapter +
                ", \ndataChapter=" + dataChapter + '}';
    }
}
