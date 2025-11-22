package org.example.Module.Google.Sheet;

public class SheetObjectModun {
    private String name;
    private String linkDriveFolder;
    private String duration;
    private String status;

    public SheetObjectModun(String name, String linkDriveFolder, String duration, String status) {
        this.name = name;
        this.linkDriveFolder = linkDriveFolder;
        this.duration = duration;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkDriveFolder() {
        return linkDriveFolder;
    }

    public void setLinkDriveFolder(String linkDriveFolder) {
        this.linkDriveFolder = linkDriveFolder;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SheetObjectModun{" +
                "name='" + name + '\'' +
                ", linkDriveFolder='" + linkDriveFolder + '\'' +
                ", duration=" + duration +
                ", status=" + status +
                '}';
    }
}
