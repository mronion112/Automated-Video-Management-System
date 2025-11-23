package org.example.Module.Local;

import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class VideoModun  {
    private String idVideo;
    private String FileName;
    private String Duration;
    public VideoModun(String idVideo, String duration) {
        this.idVideo = idVideo;
        this.Duration = duration;

    }


//    public void getVideoProperties(String getFilePath, String getFileName) {
//
//        // Path: rootFolder / folderName / folderName.mp4
//        FilePath = getFilePath;
//        FileName = getFileName;
//
//        String videoPath = FilePath + File.separator + FileName + File.separator + FileName+".mp4";
//
//        if (!Files.exists(Path.of(videoPath))) {
//            System.err.println("File không tồn tại: " + videoPath);
//            return;
//        }
//
//        System.out.println("File tồn tại: " + videoPath);
//
//        try {
//
//            // ⭐ Đây là cách gọi FFprobe đúng nhất trong Jaffree
//            FFprobeResult result = FFprobe.atPath()        // dùng ffprobe trong PATH
//                    .setShowStreams(true)                 // show stream info
//                    .setShowFormat(true)                  // show format info (PHẢI CÓ)
//                    .setInput(videoPath)       // file cần đọc
//                    .execute();                           // chạy ffprobe và trả về object
//
//            // ⭐ Jaffree có thể trả format = null nếu file lỗi hoặc thiếu flag
//            if (result.getFormat() == null) {
//                System.err.println("FFprobe không trả về format. Kết quả đầy đủ:");
//                System.out.println(result);
//                return;
//            }
//
//            // ⭐ Lấy duration từ format (an toàn nhất)
//            double duration = result.getFormat().getDuration();
//            System.out.println("Thời lượng video: " + duration + " giây");
//
//            // Logic của bạn
//            if(duration/60 > 3){
//                Duration = true;
//            }
//            else{
//                Duration = false;
//            }
//
//            // ⭐ Chỉ lưu tên file, không lưu full path
////            setFileName(getFileName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }

//    public String getFilePath() {
//        return FilePath;
//    }
//
//    public void setFilePath(String filePath) {
//        FilePath = filePath;
//    }
//
//    public String getFileName() {
//        return FileName;
//    }
//
//    public void setFileName(String fileName) {
//        FileName = fileName;
//    }

    public String getDuration() {
        return Duration;
    }


    public void setDuration(String duration) {
        Duration = duration;
    }

    @Override
    public String toString() {
        return "VideoModun{" +
                "Duration=" + Duration +
                "}\n";
    }


}
