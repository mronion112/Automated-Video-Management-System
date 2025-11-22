package org.example.TieuHoaDanController;

import org.example.TieuHoaDanModun.MenuStory;
import org.example.TieuHoaDanModun.StoryChapter;

import java.io.IOException;
import java.util.List;

public class DataStoryFromUrl {

    String urlMenu;
    MenuStory menuStory;
    public DataStoryFromUrl(String urlMenuInPut ) {
        this.urlMenu = urlMenuInPut;
        this.menuStory = null;
    }
    public void setup() throws IOException {
        menuStory = new MenuStory(urlMenu);
        menuStory.fetchData();
    }

    public String getdataStory() throws IOException {

        String dataStory = "";
        List<String> listChapterUrls = menuStory.getListChapterUrls();
        int counter = 1;
        int end  = listChapterUrls.size();
        for(String chapterUrl : listChapterUrls) {
            if(counter == end) {
                System.out.printf("\n%s\n", chapterUrl);
                StoryChapter storyChapter = new StoryChapter(chapterUrl);
                storyChapter.scrappingDataChapter();
                for(int x = 0; x < storyChapter.getDataChapter().size(); x++){
                    if(x == storyChapter.getDataChapter().size()-1){
                        break;
                    }
                    dataStory = dataStory + " " + storyChapter.getDataChapter().get(x);
                }
            }
            else {
                System.out.printf("\n%s\n", chapterUrl);
                StoryChapter storyChapter = new StoryChapter(chapterUrl);
                storyChapter.scrappingDataChapter();
                for (String data : storyChapter.getDataChapter()) {
                    dataStory = dataStory + " " + data;
                }
            }
            counter++;
        }

//        dataStory = dataStory.trim().substring(0, dataStory.length()-1);

        return dataStory.trim();
    }

    public String getTitleStory() throws IOException {
        return menuStory.getTitle();
    }

    public String getDescriptionStory() throws IOException {
        return menuStory.getDescription();
    }





}
