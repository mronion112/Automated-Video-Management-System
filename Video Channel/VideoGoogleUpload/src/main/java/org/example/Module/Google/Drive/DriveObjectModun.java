package org.example.Module.Google.Drive;

public class DriveObjectModun {
    private String folderName;
    private String folderPathDirve;
    private String duration;



    public DriveObjectModun(String folderName,String folderPathDirve  ,String duration) {
        this.folderName = folderName;
        this.folderPathDirve = folderPathDirve;
        this.duration = duration;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderPathDirve() {
        return folderPathDirve;
    }

    public void setFolderPathDirve(String folderPathDirve) {
        this.folderPathDirve = folderPathDirve;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "DriveObjectModun{" +
                ", duration=" + duration +
                "}\n";
    }
}
