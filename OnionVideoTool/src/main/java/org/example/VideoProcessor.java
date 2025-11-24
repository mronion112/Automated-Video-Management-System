package org.example;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.IOException;
import java.nio.file.*;

public class VideoProcessor {
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public VideoProcessor(String ffmpegPath, String ffprobePath) throws IOException {
        this.ffmpeg = new FFmpeg(ffmpegPath);
        this.ffprobe = new FFprobe(ffprobePath);
    }

    public void processVideo(Path input, Path outputDir) throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(input.toString());
        double duration = probeResult.getFormat().duration; // thời lượng (giây)

        System.out.printf("File: %s | Duration: %.2f sec%n", input.getFileName(), duration);

        if (duration <= 60) {
            // Nếu video ngắn hơn 1 phút, copy nguyên
            Path target = outputDir.resolve(input.getFileName());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        // Nếu dài hơn 1 phút, xử lý chia video
        String baseName = input.getFileName().toString().replaceFirst("\\.[^.]+$", "");
        String outputPattern = outputDir.resolve(baseName + "_part%d.mp4").toString();

        try {
            // ⚡ Thử chia nhanh (copy codec)
            FFmpegBuilder fastBuilder = new FFmpegBuilder()
                    .setInput(input.toString())
                    .addOutput(outputPattern)
                    .setFormat("segment")
                    .addExtraArgs("-segment_time", "60")  // chia mỗi file 60s
                    .addExtraArgs("-reset_timestamps", "1")
                    .addExtraArgs("-map", "0:v")
                    .addExtraArgs("-map", "0:a?")
                    .addExtraArgs("-start_number", "1")
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
                    .done();

            new FFmpegExecutor(ffmpeg, ffprobe).createJob(fastBuilder).run();
            int parts = (int) Math.ceil(duration / 60.0);
            System.out.printf("✅ Fast split done: %s → %d parts%n", input.getFileName(), parts);

        } catch (Exception e) {
            System.err.println("⚠️ Fast split failed (" + input.getFileName() + "), re-encoding...");

            // 🧱 Encode lại sang định dạng an toàn (H.264 + AAC)
            String safeOutput = outputDir.resolve(baseName + "_reencoded.mp4").toString();

            FFmpegBuilder reencode = new FFmpegBuilder()
                    .setInput(input.toString())
                    .addOutput(safeOutput)
                    .setVideoCodec("libx264")
                    .setAudioCodec("aac")
                    .setFormat("mp4")
                    .done();

            new FFmpegExecutor(ffmpeg, ffprobe).createJob(reencode).run();

            // Sau khi encode lại, chia lại thành 60s
            String outputPattern2 = outputDir.resolve(baseName + "_part%d.mp4").toString();

            FFmpegBuilder builder2 = new FFmpegBuilder()
                    .setInput(safeOutput)
                    .addOutput(outputPattern2)
                    .setFormat("segment")
                    .addExtraArgs("-segment_time", "60")
                    .addExtraArgs("-reset_timestamps", "1")
                    .addExtraArgs("-map", "0:v")
                    .addExtraArgs("-map", "0:a?")
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
                    .done();

            new FFmpegExecutor(ffmpeg, ffprobe).createJob(builder2).run();

            int parts = (int) Math.ceil(duration / 60.0);
            System.out.printf("✅ Re-encoded and split: %s → %d parts%n", input.getFileName(), parts);
        }
    }
}