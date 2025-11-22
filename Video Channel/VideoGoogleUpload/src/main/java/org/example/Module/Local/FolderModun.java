package org.example.Module.Local;

import java.io.File;
import java.util.List;

public class FolderModun {
    private String folderName;
    private String folderDir;
    private File folder;
    private List<FileModun> listFile;

    public FolderModun(String folderDir){
        this.folderDir = folderDir;
        folder = new File(folderDir);
        folderName = folder.getName();

    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderDir() {
        return folderDir;
    }

    public void setFolderDir(String folderDir) {
        this.folderDir = folderDir;
    }

    public List<FileModun> getListFile() {
        return listFile;
    }

    public void setListFile(List<FileModun> listFile) {
        this.listFile = listFile;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    @Override
    public String toString() {
        return "FolderModun{" +
                "folderName='" + folderName + '\'' +
                ", folderDir='" + folderDir + '\'' +
                ", folder=" + folder +
                ", listFile=" + listFile +
                '}';
    }
}
