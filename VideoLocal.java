package org.example;

public class VideoLocal {
    private String duration;
    private String filePath;

    public VideoLocal(String filePath , String duration) {
        this.filePath = filePath;
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "VideoLocal{" +
                ", filePath='" + filePath + '\'' +
                ", duration='" + duration + '\'' +
                "}\n";
    }
}
