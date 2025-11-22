package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.net.Urls;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("""
            
            ███╗   ███╗██████╗      ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗\s
                    ████╗ ████║██╔══██╗    ██╔═══██╗████╗  ██║██║████╗  ██║██╔════╝\s
                    ██╔████╔██║██████╔╝    ██║   ██║██╔██╗ ██║██║██╔██╗ ██║██║  ███╗
                    ██║╚██╔╝██║██╔══██╗    ██║   ██║██║╚██╗██║██║██║╚██╗██║██║   ██║
                    ██║ ╚═╝ ██║██║  ██║    ╚██████╔╝██║ ╚████║██║██║ ╚████║╚██████╔╝
                    ╚═╝     ╚═╝╚═╝  ╚═╝     ╚═════╝ ╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝\s
                                               Mr.Onion
        Welcome to the OnionTool download video form link BiliBili
        + We support for only https://www.bilibili.com/ website
        
        #NOTICE : Sometime the tool won't working, don't worry just run again (in Worstcase call Onion solve problem)
        However enjoy my tool ヾ(≧▽≦*)o
        
                    """);
        Scanner kb = new Scanner(System.in);

        String urlChannel = "";
        while(urlChannel.equals("")){
            System.out.print("Link Channel scrapping here : ");
            urlChannel = kb.nextLine();

            if(urlChannel.isEmpty()){
                System.out.println("Warning urlChannel must not be null, type again");
            }
        }
        String currentDir = System.getProperty("user.dir");

        System.out.print("Output folder locate to store video download (Enter will use default OutPut.folder) : ");
        String user_OutPutFolder = kb.nextLine();

//        System.out.print("File .txt to store DataScrap ( Enter will use default DataScrap.json) : ");
//        String user_File_FATH = kb.nextLine();


        String FILE_PATH = currentDir + File.separator + "Output"+File.separator+"DataScrap.json";
        String OutPutFolder = currentDir + File.separator + "Output"+ File.separator;

        File  file_Path = new File(FILE_PATH);
        File file_User = new File(user_OutPutFolder + File.separator + "DataScrap.json");

        boolean file_Null;

        // Lấy danh sách video từ kênh
        ArrayList<Video> VideoData = BiliBiliVideoStore.listUrlVideos(urlChannel);

        if(VideoData.isEmpty()){
            System.out.println("No videos found");
            return;
        }
        // Thư mục đầu ra
        String finalOutFolder = user_OutPutFolder.isEmpty() ? OutPutFolder : user_OutPutFolder;

        // Xác định file nào sẽ được dùng
        File fileToUse = user_OutPutFolder.isEmpty() ? file_Path : file_User;

        // Kiểm tra file rỗng hoặc chưa tồn tại
        if (!fileToUse.exists() || fileToUse.length() == 0) {
            file_Null = true;  // File chưa có dữ liệu
        } else {
            file_Null = false; // File đã có dữ liệu
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, VideoLocal> listVideos = new HashMap<>();

        FileWriter writer = new FileWriter(fileToUse, true);

        // Nếu chưa có file (hoặc file rỗng) → ghi toàn bộ danh sách
        if (file_Null) {    //fileToUse
            System.out.println("\nFile dữ liệu trống, đang khởi tạo mới...");


            // Tải xuống video + thumbnail
            int count = 1;
            for (Video video : VideoData) {
                System.out.println(count+"/"+VideoData.size());
                String folderPath = video.CreateFolderVideo(finalOutFolder);
                video.DownloadThumbnailBaseUrl(folderPath);
                video.DownloadVideoBaseUrl(folderPath);
                video.checkDurationVideo(folderPath);
                String duration = "";
                if(video.isDuration()){
                    duration = "Long";
                }
                else{
                    duration = "Short";
                }
                listVideos.put(video.getVideoName(), new VideoLocal(duration));

                count++;

            }

            String json = gson.toJson(listVideos);
            System.out.println(json);

            writer.write(json);
            writer.write("\n");
            writer.flush();
            System.out.println("\nHoàn tất khởi tạo dữ liệu và tải toàn bộ video.");


        } else {
            // Nếu file đã tồn tại → chỉ tải video mới
            System.out.println("\nĐang kiểm tra các video mới...");

            // Đọc dữ liệu hiện có
            JsonModun jsonData = new JsonModun(fileToUse.getAbsolutePath());
            jsonData.getJsonData();

            HashMap<String, Video> listCurrentVideos = jsonData.getLocalData();
            System.out.println("List Current Local Video\n"+listCurrentVideos);


            ArrayList<Video> NewVideoData = new ArrayList<>();
            int skipCount = 0;
            int newCount = 0;
            for(Video video : VideoData){
                if(listCurrentVideos.containsKey(video.getVideoName())){
                    System.out.println("Skip...");
                    skipCount++;
                }
                else {
                    NewVideoData.add(video);
                    System.out.println("Found new video ");
                    newCount++;
                }
            }
            System.out.println("New Video Count: "+newCount);
            System.out.println("Skip "+skipCount+"/"+VideoData.size());

            if (NewVideoData.isEmpty() || NewVideoData.size() == 0 || newCount == 0) {
                System.out.println("Không có video mới nào được thêm!");
                return;
            } else {
                System.out.println("Đã cập nhật thêm " + NewVideoData.size() + " video mới!");
                int count = 1;

                for (Video video : NewVideoData) {
                    System.out.println(count+"/"+NewVideoData.size());
                    String folderPath = video.CreateFolderVideo(finalOutFolder);
                    video.DownloadThumbnailBaseUrl(folderPath);
                    video.DownloadVideoBaseUrl(folderPath);
                    video.checkDurationVideo(folderPath);

                    String duration = "";
                    if(video.isDuration()){
                        duration = "Long";
                    }
                    else{
                        duration = "Short";
                    }

                    listVideos.put(video.getVideoName(), new VideoLocal(duration));
                    count++;

                }
            }

            String json = gson.toJson(listVideos);
            System.out.println(json);

                writer.write(json);
            writer.write("\n");
            writer.flush();


            System.out.println("\nHoàn tất cập nhật danh sách video.");
        }





    }
}