package org.example;

public class VideoLocal {
    private String duration;

    public VideoLocal(String duration) {
        this.duration = duration;
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
                ", duration='" + duration + '\'' +
                "}\n";
    }
}
