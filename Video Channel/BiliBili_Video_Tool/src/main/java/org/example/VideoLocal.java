package org.example;

public class VideoLocal {
    private String duration;
    private String fileId;

    public VideoLocal(String fileId , String duration) {
        this.fileId = fileId;
        this.duration = duration;
    }

    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
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
                ", fileId='" + fileId + '\'' +
                ", duration='" + duration + '\'' +
                "}\n";
    }
}
