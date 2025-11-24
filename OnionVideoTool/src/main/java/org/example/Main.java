package org.example;

import java.nio.file.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner kb = new Scanner(System.in);

        System.out.println("Input directory: ");
        String inputFolder = kb.nextLine();

        System.out.println("Output directory: ");
        String outputFolder = kb.nextLine();

        Path inputDir = Paths.get(inputFolder);
        Path outputDir = Paths.get(outputFolder);
        Files.createDirectories(outputDir);

        // chỉ cần trỏ tương đối nếu ffmpeg nằm cạnh file exe
        VideoProcessor processor = new VideoProcessor(
                "ffmpeg\\ffmpeg.exe",
                "ffmpeg\\ffprobe.exe"
        );

        Files.walk(inputDir)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.toString().matches(".*\\.(mp4|mov|avi|mkv)$"))
                .forEach(video -> {
                    try {
                        processor.processVideo(video, outputDir);
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                });

        System.out.println("✅ Done! Press Enter to exit...");
        System.in.read(); // 👈 giữ cửa sổ CMD mở
    }
}
