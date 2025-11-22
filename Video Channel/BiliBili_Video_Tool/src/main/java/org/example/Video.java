package org.example;

import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Video implements DownloadVideo, DownloadThumbnail, CreateFolderVideo, CheckDurationVideo {

    // Note Exe add
    public static String ytDlpPath = "yt-dlp.exe";
    private String videoName;
    private String videoUrl;
    private boolean Duration;

    //Note Exe add
    public static File ffmpegPath = new File("ffmpeg" + File.separator + "ffmpeg.exe");



    public Video(String videoName, String videoUrl) {
        this.videoName = videoName;
        this.videoUrl = videoUrl;
    }


    public boolean isDuration() {
        return Duration;
    }

    public void setDuration(boolean duration) {
        Duration = duration;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }


    @Override
    public void checkDurationVideo(String FilePath) {

        String videoPath = FilePath +File.separator + videoName +".mp4";

        File file = new File(videoPath);

        try {
            if (!Files.exists(Path.of(videoPath))) {
                System.err.println("File không tồn tại: " + videoPath);
                return;
            }
        }catch(InvalidPathException e){
            System.out.println("Đường dẫn file có ký tự đặc biệt | ! @ #...");
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("File tồn tại: " + videoPath);

        try {

            // ⭐ Đây là cách gọi FFprobe đúng nhất trong Jaffree
            FFprobeResult result = FFprobe.atPath()
                    .setShowStreams(true)
                    .setShowFormat(true)
                    .setInput(videoPath)
                    .execute();

            // ⭐ Jaffree có thể trả format = null nếu file lỗi hoặc thiếu flag
            if (result.getFormat() == null) {
                System.err.println("FFprobe không trả về format. Kết quả đầy đủ:");
                System.out.println(result);
                return;
            }

            // ⭐ Lấy duration từ format (an toàn nhất)
            float duration = result.getFormat().getDuration();
            int hours = (int) (duration / 3600);
            int minutes = (int) ((duration % 3600) / 60);
            int seconds = (int) (duration % 60);

            System.out.printf("Thời lượng video: %02d:%02d:%02d%n", hours, minutes, seconds);
            // Logic của bạn
            if(duration > 180){
                Duration = true;
            }
            else{
                Duration = false;
            }

            // ⭐ Chỉ lưu tên file, không lưu full path
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public String CreateFolderVideo(String outputFolder) throws InterruptedException, IOException {
        File  folder = new File(outputFolder+ File.separator + videoName);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                System.out.println("Đã tạo thư mục: " + folder.getAbsolutePath());
            } else {
                System.out.println("Không thể tạo thư mục: " + folder.getAbsolutePath());
            }
        } else {
            System.out.println("Thư mục đã tồn tại: " + folder.getAbsolutePath());
        }

        return  folder.getAbsolutePath();
    }


    @Override
    public void DownloadThumbnailBaseUrl(String outputFolder) throws InterruptedException, IOException {
        // Lưu dạng: outputFolder + title + .jpg
        String Thumbnail_Path = outputFolder + File.separator + "%(title)s.%(ext)s";
        ProcessBuilder pb = new ProcessBuilder(
                ytDlpPath,
//                "--quiet",
//                "--no-warnings",
//                "--print", "none",
                "--progress",
                "--skip-download",         // Không tải video
                "--write-thumbnail",       // Chỉ tải thumbnail
                "--convert-thumbnails", "jpg",
                "-o", Thumbnail_Path,  // Đường dẫn file
                videoUrl                   // URL video thumnail
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Đã tải thumbnail thành công cho video: ");
        } else {
            System.out.println("Lỗi khi tải thumbnail (exit code " + exitCode + ")");
        }
    }


    @Override
    public void DownloadVideoBaseUrl(String outPutFolder) throws InterruptedException, IOException {

        if (!ffmpegPath.exists()) {
            System.out.println("Không tìm thấy ffmpeg ở: " + ffmpegPath.getAbsolutePath());
            return;
        }

        String outputTemplate = outPutFolder + File.separator + "%(title)s.%(ext)s";

        ProcessBuilder pb = new ProcessBuilder(
                ytDlpPath,
                "--progress",
                "--ffmpeg-location", ffmpegPath.getAbsolutePath(),
                "-f", "bestvideo+bestaudio",
                "-o", outputTemplate,
                "--newline",
                // add Exe here
                "--external-downloader", "aria2c.exe",
                "--external-downloader-args", "-x 16 -s 24 -k 4M",
                videoUrl
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Regex yt-dlp progress (vd: 91% | 106MiB | 8.7MiB/s | ETA: 1s)
        Pattern ytDlpPattern = Pattern.compile(
                "(\\d+)% \\| (\\S+) \\| (\\S+)/s \\| ETA: (\\S+)"
        );

        // Regex aria2c progress (vd: [#236375 115MiB/116MiB(99%) CN:4 DL:8.6MiB ETA:1s])
        Pattern aria2cPattern = Pattern.compile(
                "\\[#.+?\\s+(\\S+)/\\S+\\((\\d+)%\\)\\s+CN:\\d+\\s+DL:(\\S+)\\s+ETA:(\\S+)\\]"
        );

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {

                Matcher m1 = ytDlpPattern.matcher(line);
                Matcher m2 = aria2cPattern.matcher(line);

                if (m1.find()) {
                    String percent = m1.group(1);
                    String size = m1.group(2);
                    String speed = m1.group(3);
                    String eta = m1.group(4);

                    System.out.printf("\r%s%% | %s | %s/s | ETA: %s   ",
                            percent, size, speed, eta);
                    System.out.flush();
                }
                else if (m2.find()) {
                    String size = m2.group(1);
                    String percent = m2.group(2);
                    String speed = m2.group(3);
                    String eta = m2.group(4);

                    System.out.printf("\r%s%% | %s | %s/s | ETA: %s   ",
                            percent, size, speed, eta);
                    System.out.flush();
                }
                else {
                    System.out.println(line);
                }
            }
        }

        int exitCode = process.waitFor();
        System.out.println("\nTải xong (exit code " + exitCode + ")");


        // 🎯 Sau khi tải xong: kiểm tra nếu bị tách file -> tự merge
        autoMergeIfSeparated(outPutFolder, ffmpegPath);
    }

    /**
     * Nếu trong thư mục có cả file video và audio riêng biệt thì tự động hợp nhất lại.
     */
    private void autoMergeIfSeparated(String outputFolder, File ffmpegPath) {
        File folder = new File(outputFolder);
        File[] files = folder.listFiles((dir, name) ->
                name.matches(".*\\.(mp4|mkv|webm|m4a|opus)$"));

        if (files == null || files.length == 0) return;

        File videoFile = null;
        File audioFile = null;

        for (File f : files) {
            String name = f.getName().toLowerCase();
            if (name.endsWith(".m4a") || name.endsWith(".opus")) {
                audioFile = f;
            } else if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".webm")) {
                videoFile = f;
            }
        }

        if (videoFile != null && audioFile != null) {
            System.out.println("Phát hiện video/audio tách riêng — tiến hành hợp nhất...");

            // Tên file đầu ra
            File merged = new File(folder, "Merged_" + videoFile.getName().replaceAll("\\..+$", ".mp4"));

            try {
                ProcessBuilder mergePb = new ProcessBuilder(
                        ffmpegPath.getAbsolutePath(),
                        "-i", videoFile.getAbsolutePath(),
                        "-i", audioFile.getAbsolutePath(),
                        "-c", "copy",
                        merged.getAbsolutePath()
                );
                mergePb.redirectErrorStream(true);
                Process mergeProcess = mergePb.start();

                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(mergeProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) System.out.println(line);
                }

                int code = mergeProcess.waitFor();
                if (code == 0) {
                    System.out.println("Hợp nhất thành công: " + merged.getName());
                    // Xóa file gốc sau khi merge
                    videoFile.delete();
                    audioFile.delete();
                } else {
                    System.out.println("Lỗi khi hợp nhất (exit code " + code + ")");
                }
            } catch (Exception e) {
                System.out.println("Lỗi merge: " + e.getMessage());
            }
        } else {
            System.out.println("Không phát hiện file tách riêng, bỏ qua merge.");
        }
    }


    @Override
    public String toString() {
        return "Video{" +
                "videoName='" + videoName + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", Duration=" + Duration +
                "}\n";
    }
}
