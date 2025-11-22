package org.example.TieuHoaDanModun;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuStory  {
    private String urlMenuStory;
    private Document document;
    private String title;
    private String description;
    private List<String> types;
    private List<String> listChapterUrls;

    public MenuStory(String urlMenuStory) {
        this.urlMenuStory = urlMenuStory;
        this.document = null;
        this.title = "";
        this.types = null;
        this.description = null;
        this.listChapterUrls = null;
    }
    public void fetchData() throws IOException {
        try {
            this.document = Jsoup.connect(this.urlMenuStory)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            if (this.document != null) {
                System.out.println("Document successfully loaded");
            }
        } catch (IOException e) {
            System.err.println("Fail to fetch document: " + e.getMessage());
        }
        if(this.document == null) {
            System.out.println("Fail to fetch document");
            return;
        }


        //Create title
        Elements titleEl = document.select("h3.story-name");
        if (!titleEl.isEmpty()) {
            this.title = titleEl.get(0).text();
        }

        if (this.title == null) {
            System.out.printf("Title is null\n");
        }

        //Create description
        Elements descEls = document.select("div.story-detail__top--desc.px-3 p");
        StringBuilder desc = new StringBuilder();
        for (Element el : descEls) {
            desc.append(el.text()).append(" ");
        }
        this.description = desc.toString().trim().replaceAll("[/\\\\~#*]", "").
                                                replace("c h ế t","chết").
                                                replace("c h ó", "chó");
        ;

        if (this.description == null) {
            System.out.printf("Description is null\n");
        }

        //Create Types
        List<String> typeList = new ArrayList<>();
        Elements typeEls = document.select("div.d-flex.align-items-center.flex-warp a");
        for (Element el : typeEls) {
            String text = el.text().replace(",", "").trim();
            if (text.contains("Ngôn Tình") || text.contains("Hiện Đại")) {
                typeList.add(text);
            }

        }
        this.types = typeList;


        if (types.isEmpty()) {
            System.out.printf("Types is null\n");
        }

        //Create ListUrlChapter
        List<String> urls = new ArrayList<>();
        Elements chapterLinks = document.select("div.story-detail__list-chapter--list__item a");
        for (Element link : chapterLinks) {
            urls.add(link.absUrl("href"));
        }
        this.listChapterUrls = urls;

        if (listChapterUrls == null || listChapterUrls.size() == 0) {
            System.out.printf("Chapter urls is null\n");
        }
    }

    public void setUrlMenuStory(String urlMenuStory) {
        this.urlMenuStory = urlMenuStory;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setListChapterUrls(List<String> listChapterUrls) {
        this.listChapterUrls = listChapterUrls;
    }

    public String getUrlMenuStory() {
        return urlMenuStory;
    }

    public Document getDocument() {
        return document;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getListChapterUrls() {
        return listChapterUrls;
    }

    @Override
    public String toString() {
        return "MenuStory{" +
                "\nurlMenuStory='" + urlMenuStory + '\'' +
                ", \ntitle='" + title + '\'' +
                ", \ntypes='" + types.toString() + '\'' +
                ", \ndescription='" + description + '\'' +
                ", \nlistChapterUrls=" + listChapterUrls.toString()  +
                '}';
    }
}